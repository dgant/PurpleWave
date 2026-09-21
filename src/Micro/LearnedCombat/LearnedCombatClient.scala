package Micro.LearnedCombat

import Lifecycle.{PurpleBWClient, With}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import mjson.Json

import java.io._
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{SelectionKey, Selector, SocketChannel}
import java.nio.charset.StandardCharsets
import scala.collection.mutable

/** Nonblocking IO runs on the game thread; inference remains in the service. */
final class LearnedCombatClient {
  private val config = With.configuration
  private val mode = config.learnedCombatMode
  private val enabled = mode != "off"
  private val mayApply = mode == "eval" || mode == "sample"
  private val host = config.learnedCombatHost
  private val port = config.learnedCombatPort
  private val timeoutMs = config.learnedCombatTimeoutMs
  // Async execution holds a recent learned decision and blocks before it expires.
  private val synchronous = config.learnedCombatSynchronous
  private val maxAge = config.learnedCombatMaxAgeFrames
  private val period = config.learnedCombatDecisionFrames
  private val observation = new LearnedCombatObservation
  private val audits = new mutable.ArrayBuffer[String]
  private val proposed = new mutable.HashMap[(Long, Long), Json]
  private var sequence = 0L
  private var lastCapture = -1000000
  private var captureSequence = -1L
  private var active: Option[Result] = None
  private var inFlight: Option[Request] = None
  private var hello = ""
  private var connection: LearnedCombatConnection = _
  private var writer: PrintWriter = _
  private var ready = false
  private var stopped = false
  private var failure: String = ""
  private var version = -1L
  private var lastReportedFailure = ""

  private case class Choice(logit: Float, action: Boolean, logProbability: Float)
  private case class Result(sequence: Long, frame: Int, version: Long, durationMs: Float, choices: Map[(Long, Long), Choice])
  private case class Request(payload: Array[Byte], captureMs: Double, captureTimings: Map[String, Double], captureWallMs: Long, idleAtCaptureMs: Double, queuedNanos: Long = System.nanoTime())

  // JBWAPI records this on the bot thread while waiting for a new game frame.
  // Keep the raw request clock as well: idle time is not inference work.
  private def gameIdleMillis: Double = {
    val total = PurpleBWClient.getPerformanceMetrics.getBotIdle.getRunningTotal
    total.getSamples * total.getMean
  }

  def onStart(): Unit = {
    if (!enabled) return
    require(Set("record", "shadow", "eval", "sample").contains(mode), "Invalid learnedCombatMode")
    require(!mayApply || config.fightOrFleeEstimationOnly, "Learned policy application requires fightOrFleeEstimationOnly")
    require(period > 0 && maxAge >= 0 && timeoutMs > 0 && port > 0 && port < 65536, "Invalid learned combat timing/address")
    require(!mayApply || (maxAge <= 4 && period <= maxAge), "Learned combat decisions must stay within four game frames")
    observation.initialize()
    val gameId = if (config.learnedCombatGameId.nonEmpty) config.learnedCombatGameId else With.id.toString()
    hello = Json.`object`().set("game_id", gameId).set("mode", mode)
      .set("schema_hash", LearnedCombatObservation.SchemaHash).set("token", config.learnedCombatToken)
      .set("player_id", With.self.id).set("player", With.self.name)
      .set("map", With.mapFileName).set("map_hash", With.game.mapHash())
      .set("opponent", With.enemies.map(_.name).mkString(","))
      .set("estimation_only", config.fightOrFleeEstimationOnly).set("synchronous", synchronous)
      .set("reference_kind", if (config.learnedCombatFullTeacherLabels || !config.fightOrFleeEstimationOnly) "full_rules" else "estimator")
      .set("decision_contract", "hold-or-block-v1")
      .set("jvm_version", System.getProperty("java.runtime.version"))
      .set("jvm_processors", Runtime.getRuntime.availableProcessors())
      .set("experiment_seed", config.experimentSeed).set("decision_frames", period).set("max_age_frames", maxAge)
      .set("oracle_path", config.learnedCombatOraclePath).toString()
    val eventPath = new File(With.bwapiData.write, "pwcombat-client.jsonl")
    try {
      writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(eventPath), StandardCharsets.UTF_8)))
      writer.println(Json.read(hello).set("token", "[redacted]").toString())
      writer.flush()
      connection = new LearnedCombatConnection(host, port, timeoutMs)
      connection.send(1, hello.getBytes(StandardCharsets.UTF_8))
      val acknowledgment = connection.receive(block = true).get
      if (acknowledgment._1 != 2) throw new IOException("missing service handshake")
      val readyMessage = Json.read(new String(acknowledgment._2, StandardCharsets.UTF_8))
      if (readyMessage.at("schema_hash").asString() != LearnedCombatObservation.SchemaHash) throw new IOException("schema mismatch")
      version = readyMessage.at("version").asLong()
      ready = true
    } catch { case error: Exception => fail(error.toString()) }
    With.logger.debug("Learned combat: " + mode + " at " + host + ":" + port)
  }

  def onFrame(): Unit = {
    if (failure.nonEmpty && failure != lastReportedFailure) {
      With.logger.warn("Learned combat fallback: " + failure)
      lastReportedFailure = failure
      active = None
      proposed.clear()
    }
  }

  def onCycle(): Unit = {
    if (!enabled || stopped || failure.nonEmpty) return
    try {
      completePrediction(false, "cycle")
      if (synchronous || (mayApply && active.isEmpty)) awaitReady()
      if (!ready || inFlight.nonEmpty || With.frame - lastCapture < period) return
      requestPrediction("cycle", synchronous || (mayApply && active.isEmpty))
    } catch { case error: Exception => fail(error.toString()) }
  }

  private def awaitReady(): Unit = {
    if (!ready || failure.nonEmpty) throw new IOException("inference unavailable: " + failure)
  }

  private def usableResult: Option[Result] =
    active.filter(r => r.frame <= With.frame && With.frame - r.frame <= maxAge)

  private def completePrediction(block: Boolean, reason: String, waitStart: Long = System.nanoTime()): Unit = {
    inFlight.foreach { request =>
      connection.receive(block).foreach { response =>
        if (response._1 != 4) throw new IOException("missing inference response")
        val result = decodeResult(response._2)
        val sent = new DataInputStream(new ByteArrayInputStream(request.payload))
        if (result.sequence != sent.readLong() || result.frame != sent.readInt() || result.version != version)
          throw new IOException("response identity/version mismatch")
        val captureTimings = Json.`object`()
        request.captureTimings.foreach { case (name, value) => captureTimings.set(name, value) }
        val gameIdleMs = Math.max(0.0, gameIdleMillis - request.idleAtCaptureMs)
        val totalMs = request.captureMs + (connection.responseNanos - request.queuedNanos) / 1000000.0
        writer.println(Json.`object`().set("kind", "response").set("sequence", result.sequence)
          .set("frame", result.frame).set("version", result.version).set("inference_ms", result.durationMs)
          .set("capture_wall_ms", request.captureWallMs).set("send_wall_ms", connection.sendWallMs)
          .set("sent_wall_ms", connection.sentWallMs).set("response_wall_ms", connection.responseWallMs)
          .set("write_ms", (connection.sentNanos - connection.sendNanos) / 1000000.0)
          .set("read_ms", (connection.responseNanos - connection.sentNanos) / 1000000.0)
          .set("capture_phases_ms", captureTimings)
          .set("capture_ms", request.captureMs).set("queue_ms", (connection.sendNanos - request.queuedNanos) / 1000000.0)
          .set("request_prepare_ms", (connection.sendNanos - request.queuedNanos) / 1000000.0)
          .set("offer_ms", 0.0).set("handoff_ms", 0.0)
          .set("roundtrip_ms", (connection.responseNanos - connection.sendNanos) / 1000000.0)
          .set("total_ms", totalMs).set("game_idle_ms", gameIdleMs)
          .set("active_total_ms", Math.max(0.0, totalMs - gameIdleMs)).toString())
        writer.flush()
        active = Some(result)
        inFlight = None
        val readyTotalMs = request.captureMs + (System.nanoTime() - request.queuedNanos) / 1000000.0
        audits += Json.`object`().set("kind", "inference_ready").set("frame", With.frame)
          .set("sequence", captureSequence).set("observed_frame", result.frame)
          .set("ready_total_ms", readyTotalMs).set("game_idle_ms", gameIdleMs)
          .set("ready_active_ms", Math.max(0.0, readyTotalMs - gameIdleMs)).toString()
      }
      if (block) audits += Json.`object`().set("kind", "inference_wait").set("frame", With.frame)
        .set("sequence", captureSequence).set("reason", reason)
        .set("wait_ms", (System.nanoTime() - waitStart) / 1000000.0).toString()
    }
  }

  private def requestPrediction(reason: String, block: Boolean): Unit = {
    require(inFlight.isEmpty, "only one recurrent request may be in flight")
    val idleAtCaptureMs = gameIdleMillis
    val captureWallMs = System.currentTimeMillis()
    val waitStart = System.nanoTime()
    val payload = observation.capture(sequence, audits.toVector)
    audits.clear()
    val request = Request(payload, (System.nanoTime() - waitStart) / 1000000.0, observation.captureTimings, captureWallMs, idleAtCaptureMs)
    connection.send(3, payload)
    inFlight = Some(request)
    captureSequence = sequence
    sequence += 1
    lastCapture = With.frame
    completePrediction(block, reason, waitStart)
  }

  def beforeAgent(unit: FriendlyUnitInfo): Unit = {
    if (!mayApply || stopped || failure.nonEmpty || !unit.canMove || unit.battle.isEmpty) return
    val key = LearnedCombatObservation.identity(unit)
    // Capture before execute() resets destinations or the estimator overwrites
    // shouldFight, so previous-action features retain their declared meaning.
    try {
      awaitReady()
      completePrediction(false, "combat_ready")
      if (inFlight.isEmpty && With.frame - lastCapture >= period)
        requestPrediction("combat_refresh", synchronous || active.isEmpty)
      if (usableResult.flatMap(_.choices.get(key)).isEmpty) {
        // A late request may itself already be stale after the game advanced.
        // Finish it in sequence, then capture the current state if necessary.
        completePrediction(true, "combat_deadline")
        if (usableResult.flatMap(_.choices.get(key)).isEmpty && With.frame > lastCapture)
          requestPrediction("combat_missing", block = true)
        if (usableResult.flatMap(_.choices.get(key)).isEmpty)
          throw new IOException("missing combat prediction after blocking refresh")
      }
    } catch { case error: Exception => fail(error.toString()) }
  }

  def onEstimate(unit: FriendlyUnitInfo): Unit = {
    if (!enabled || stopped || failure.nonEmpty) return
    val key = LearnedCombatObservation.identity(unit)
    val reference = unit.agent.shouldFight
    val referenceReason = unit.agent.fightReason
    if (mayApply && unit.battle.isDefined && usableResult.flatMap(_.choices.get(key)).isEmpty)
      fail("missing combat prediction at application; beforeAgent was not satisfied")
    val usable = if (failure.isEmpty) usableResult else None
    val choice = usable.flatMap(_.choices.get(key))
    val applies = failure.isEmpty && mayApply && choice.isDefined && unit.battle.isDefined
    if (applies) {
      unit.agent.shouldFight = choice.get.action
      unit.agent.fightReason = "Learned"
    }
    val event = Json.`object`().set("kind", "decision").set("frame", With.frame)
      .set("id", key._1).set("generation", key._2).set("sequence", usable.map(_.sequence).getOrElse(captureSequence))
      .set("in_battle", unit.battle.isDefined).set("baseline", reference).set("baseline_reason", referenceReason).set("applied", applies)
      .set("reference_kind", if (config.learnedCombatFullTeacherLabels || !config.fightOrFleeEstimationOnly) "full_rules" else "estimator")
      .set("fallback", if (failure.nonEmpty) failure else if (mode != "record" && choice.isEmpty) "missing_or_stale" else "")
    choice.foreach(c => event.set("logit", c.logit).set("sampled", c.action).set("log_probability", c.logProbability))
    usable.foreach(r => event.set("source_frame", r.frame).set("policy_version", r.version).set("inference_ms", r.durationMs))
    proposed.put(key, event)
  }

  def afterAgent(unit: FriendlyUnitInfo): Unit = {
    if (!enabled || stopped) return
    observation.recordDecision(unit)
    proposed.remove(LearnedCombatObservation.identity(unit)).foreach(event => {
      event.set("final_fight", unit.agent.shouldFight).set("final_reason", unit.agent.fightReason)
      val command = unit.bwapiUnit.getLastCommand
      event.set("command_frame", unit.bwapiUnit.getLastCommandFrame)
      if (command != null) {
        event.set("command", command.getType.toString())
        val position = command.getTargetPosition
        if (position != null) event.set("command_x", position.getX).set("command_y", position.getY)
        if (command.getTarget != null) event.set("command_target", command.getTarget.getID)
      }
      if (audits.size >= 8192) fail("audit queue overflow") else audits += event.toString()
    })
  }

  def onEnd(isWinner: Boolean): Unit = {
    if (!enabled || stopped) return
    try {
      if (ready && failure.isEmpty) {
        completePrediction(true, "end")
        val end = Json.`object`().set("frame", With.frame).set("is_winner", isWinner)
          .set("audits", Json.read("[" + audits.mkString(",") + "]")).set("client_failure", failure)
        connection.send(5, end.toString().getBytes(StandardCharsets.UTF_8))
        if (connection.receive(block = true).get._1 != 6) throw new IOException("missing end acknowledgment")
      }
    } catch { case error: Exception => fail(error.toString()) }
    finally {
      stopped = true
      ready = false
      if (connection != null) connection.close()
      if (writer != null) { writer.println(Json.`object`().set("kind", "closed").set("failure", failure).toString()); writer.close() }
    }
  }

  private def fail(message: String): Unit = {
    if (failure.isEmpty) failure = message
    if (connection != null) connection.close()
  }

  private def decodeResult(payload: Array[Byte]): Result = {
    val in = new DataInputStream(new ByteArrayInputStream(payload))
    val seq = in.readLong()
    val frame = in.readInt()
    val count = in.readInt()
    val version = in.readLong()
    val ms = in.readFloat()
    if (seq < 0 || frame < 0 || count < 0 || count > 2048 || payload.length != 28 + count * 28 || !java.lang.Float.isFinite(ms))
      throw new IOException("invalid inference result")
    val rows = (0 until count).map(_ => {
      val key = (in.readLong(), in.readLong())
      val logit = in.readFloat()
      val action = in.readFloat()
      val lp = in.readFloat()
      if (!java.lang.Float.isFinite(logit) || !java.lang.Float.isFinite(lp) || (action != 0.0f && action != 1.0f) || lp > 0.00001f)
        throw new IOException("nonfinite or invalid policy output")
      (key, Choice(logit, action == 1.0f, lp))
    })
    if (rows.map(_._1).distinct.size != count) throw new IOException("duplicate prediction identity")
    Result(seq, frame, version, ms, rows.toMap)
  }
}

/** One ordered packet exchange with bounded blocking and partial-packet polling. */
private[LearnedCombat] final class LearnedCombatConnection(host: String, port: Int, timeoutMs: Int) {
  private val channel = SocketChannel.open()
  private val selector = Selector.open()
  private var key: SelectionKey = _
  private var outgoing = Array.empty[ByteBuffer]
  private val header = ByteBuffer.allocate(16)
  private var body: ByteBuffer = _
  private var responseKind = 0
  private var waiting = false
  private var deadline = 0L
  var sendNanos = 0L
  var sentNanos = 0L
  var responseNanos = 0L
  var sendWallMs = 0L
  var sentWallMs = 0L
  var responseWallMs = 0L

  try {
    channel.configureBlocking(false)
    channel.socket().setTcpNoDelay(true)
    key = channel.register(selector, SelectionKey.OP_CONNECT)
    deadline = System.nanoTime() + timeoutMs.toLong * 1000000L
    if (!channel.connect(new InetSocketAddress(host, port))) {
      while (!channel.finishConnect()) awaitEvents(SelectionKey.OP_CONNECT)
    }
    key.interestOps(0)
  } catch { case error: Exception => close(); throw error }

  def send(kind: Int, payload: Array[Byte]): Unit = {
    require(!waiting, "one packet exchange at a time")
    sendNanos = System.nanoTime()
    sendWallMs = System.currentTimeMillis()
    deadline = sendNanos + timeoutMs.toLong * 1000000L
    sentNanos = 0L
    val prefix = ByteBuffer.allocate(16)
    prefix.putInt(0x50574342).putInt(1).putInt(kind).putInt(payload.length).flip()
    outgoing = Array(prefix, ByteBuffer.wrap(payload))
    header.clear()
    body = null
    waiting = true
    writeAvailable()
  }

  private def writeAvailable(): Unit = {
    if (sentNanos == 0L) {
      channel.write(outgoing)
      if (!outgoing.exists(_.hasRemaining)) {
        sentNanos = System.nanoTime()
        sentWallMs = System.currentTimeMillis()
      }
    }
  }

  def receive(block: Boolean): Option[(Int, Array[Byte])] = {
    require(waiting, "no packet exchange in progress")
    while (true) {
      if (System.nanoTime() >= deadline) throw new IOException("inference deadline exceeded")
      writeAvailable()
      if (body == null) {
        if (channel.read(header) < 0) throw new EOFException("inference connection closed")
        if (!header.hasRemaining) {
          header.flip()
          if (header.getInt() != 0x50574342 || header.getInt() != 1) throw new IOException("protocol mismatch")
          responseKind = header.getInt()
          val size = header.getInt()
          if (size < 0 || size > 8 * 1024 * 1024) throw new IOException("invalid response size")
          body = ByteBuffer.allocate(size)
        }
      }
      if (body != null) {
        if (body.hasRemaining && channel.read(body) < 0) throw new EOFException("inference connection closed")
        if (!body.hasRemaining) {
          responseNanos = System.nanoTime()
          responseWallMs = System.currentTimeMillis()
          waiting = false
          key.interestOps(0)
          if (sentNanos == 0L) throw new IOException("response before request was sent")
          if (responseKind == 7) throw new IOException(new String(body.array(), StandardCharsets.UTF_8))
          return Some((responseKind, body.array()))
        }
      }
      if (!block) return None
      awaitEvents(SelectionKey.OP_READ | (if (sentNanos == 0L) SelectionKey.OP_WRITE else 0))
    }
    None
  }

  private def awaitEvents(operations: Int): Unit = {
    val remaining = deadline - System.nanoTime()
    if (remaining <= 0 || Thread.currentThread().isInterrupted) throw new IOException("inference deadline exceeded")
    key.interestOps(operations)
    selector.select(Math.max(1L, (remaining + 999999L) / 1000000L))
    selector.selectedKeys().clear()
  }

  def close(): Unit = {
    try channel.close() catch { case _: IOException => () }
    try selector.close() catch { case _: IOException => () }
  }
}
