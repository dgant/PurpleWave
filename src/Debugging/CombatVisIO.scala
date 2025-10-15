package Debugging

import Information.Battles.Types.Battle
import Lifecycle.With

import java.io.{DataOutputStream, File, FileOutputStream}
import java.nio.charset.StandardCharsets
import scala.collection.mutable.ArrayBuffer

object CombatVisIO {

  @volatile private var launched: Boolean = false
  @volatile private var visProcess: Process = _

  private def baseDir: File = new File(With.bwapiData.write)
  private def opponentFile: File = {
    val opp = With.enemy.name.replaceAll("[^A-Za-z0-9._-]", "_")
    new File(baseDir, opp + ".pwcs")
  }

  @volatile private var lastDumpMs: Long = 0L
  @volatile private var lastDumpGameFrame: Int = -1000000
  @volatile private var gameStartEpochSeconds: Long = 0L
  // Throttle compressed dump enqueues to avoid IO queue flooding
  private val dumpScheduled = new java.util.concurrent.atomic.AtomicBoolean(false)
  private val nextDumpAllowedMs = new java.util.concurrent.atomic.AtomicLong(0L)
  // Diagnostics: count of successful PW2 appends to .pwcs
  private val simAppendCounter = new java.util.concurrent.atomic.AtomicInteger(0)
  // Coalescing state for live .pwcs appends: avoid near-duplicate sims (same participants) within 10s
  @volatile private var lastAppendStartFrame: Int = -1000000
  @volatile private var lastAppendParticipantsKey: String = ""
  @volatile private var lastPwcsAppendGameFrame: Int = -1000000

  // Background IO workers to avoid blocking the main game thread
  // Fast lane for lightweight .pwcs appends
  private val ioQueue = new java.util.concurrent.LinkedBlockingQueue[Runnable]()
  private val ioThread: Thread = {
    val t = new Thread(new Runnable { override def run(): Unit = {
      while (true) {
        try { val r = ioQueue.take(); r.run() } catch { case _: Throwable => }
      }
    }})
    t.setName("CombatVisIO-IO-Append")
    t.setDaemon(true)
    t.start()
    t
  }
  private def enqueueIO(r: Runnable): Unit = { try ioQueue.offer(r) catch { case _: Throwable => } }

  // Slow lane for heavy compressed .pwsim dumps (kept separate so appends are never starved)
  private val dumpQueue = new java.util.concurrent.LinkedBlockingQueue[Runnable]()
  private val dumpThread: Thread = {
    val t = new Thread(new Runnable { override def run(): Unit = {
      while (true) {
        try { val r = dumpQueue.take(); r.run() } catch { case _: Throwable => }
      }
    }})
    t.setName("CombatVisIO-IO-Dump")
    t.setDaemon(true)
    t.start()
    t
  }
  private def enqueueDump(r: Runnable): Unit = { try dumpQueue.offer(r) catch { case _: Throwable => } }

  def ensureDirs(): Unit = {
    try {
      if (!baseDir.exists()) baseDir.mkdirs()
      if (gameStartEpochSeconds == 0L) {
        gameStartEpochSeconds = System.currentTimeMillis() / 1000L
      }
    } catch { case exception: Exception => With.logger.quietlyOnException(exception) }
  }

  def exportMapData(): Unit = {
    try {
      ensureDirs()
      val hash = With.game.mapHash()
      val outFile = new File(baseDir, hash + ".mapbin")
      if (outFile.exists()) return
      val dos = new DataOutputStream(new FileOutputStream(outFile))
      try {
        val tileW = With.mapTileWidth
        val tileH = With.mapTileHeight
        val walkW = With.mapWalkWidth
        val walkH = With.mapWalkHeight
        // Header
        dos.writeInt(tileW)
        dos.writeInt(tileH)
        dos.writeInt(walkW)
        dos.writeInt(walkH)
        // Ground heights per tile (byte per tile)
        var y = 0
        while (y < tileH) {
          var x = 0
          while (x < tileW) {
            val h = With.game.getGroundHeight(x, y) & 0xFF
            dos.writeByte(h)
            x += 1
          }
          y += 1
        }
        // Walkability per mini-tile (byte per walk tile)
        var wy = 0
        while (wy < walkH) {
          var wx = 0
          while (wx < walkW) {
            val w = if (With.game.isWalkable(wx, wy)) 1 else 0
            dos.writeByte(w)
            wx += 1
          }
          wy += 1
        }
      } finally {
        dos.close()
      }
    } catch { case exception: Exception => With.logger.quietlyOnException(exception) }
  }

  def prepareEmptySimFile(): Unit = {
    try {
      ensureDirs()
      val hash = With.game.mapHash()
      val f = opponentFile
      val content = new StringBuilder()
      content.append("PW2\n").append("H|").append(hash).append('\n').append("Z\n")
      val fos = new FileOutputStream(f, false)
      try { fos.write(content.toString().getBytes(StandardCharsets.UTF_8)) } finally { try fos.close() catch { case _: Throwable => } }
    } catch { case exception: Exception => With.logger.quietlyOnException(exception) }
  }

  // framesLog: lines of format: frame|id,isFriendly,x,y,alive,width,height;
  // Prepared variant that avoids With.* access on IO thread
  private def writeSimulationLogPrepared(outPath: String, mapHash: String, framesLog: ArrayBuffer[String], startGameFrame: Int): Unit = {
    try {
      val t0 = System.nanoTime()

      // Skip appending trivial sims (no attacks or deaths). Prevents flooding the .pwcs with 1-frame micro-sims.
      var interesting = false
      var li_guard = 0
      while (li_guard < framesLog.length && !interesting) {
        val line = framesLog(li_guard)
        val parts = line.split("\\|")
        if (parts.length >= 3) {
          val tokens = parts(2).split(';')
          var ti = 0
          while (ti < tokens.length && !interesting) {
            val t = tokens(ti)
            if (t.nonEmpty && t.length >= 2 && t.charAt(1) == ':' && (t.charAt(0) == 'a' || t.charAt(0) == 'd')) {
              interesting = true
            }
            ti += 1
          }
        }
        li_guard += 1
      }
      if (!interesting) return

      // Parse metas: collect id, side, and unit type ID from first frame
      case class Meta(id: Int, friendly: Boolean, typeId: Int)
      val metas: Vector[Meta] = {
        val buf = new scala.collection.mutable.ArrayBuffer[Meta]()
        framesLog.headOption.foreach { line =>
          val parts = line.split("\\|")
          if (parts.length >= 2) {
            val units = parts(1).split(';')
            var i = 0
            while (i < units.length) {
              val u = units(i)
              if (u.nonEmpty) {
                val f = u.split(',')
                if (f.length >= 14) {
                  val id = try f(0).toInt catch { case _: Exception => -1 }
                  val friendly = f(1).toBoolean
                  val tpeName = try f(14) catch { case _: Exception => f.lastOption.getOrElse("") }
                  val typeId = try bwapi.UnitType.valueOf(tpeName).id catch { case _: Throwable => -1 }
                  if (id >= 0) buf += Meta(id, friendly, typeId)
                }
              }
              i += 1
            }
          }
        }
        buf.distinct.sortBy(_.id).toVector
      }

      // Coalescing: skip near-duplicate sims (same participants) within cooldown of last append
      val participantsKey = metas.map(_.id).sorted.mkString(",")
      val withinLastAppendCooldown = startGameFrame - lastAppendStartFrame < With.configuration.simulationRecordingPeriod
      if (withinLastAppendCooldown && participantsKey == lastAppendParticipantsKey) return
      if (startGameFrame - lastPwcsAppendGameFrame < With.configuration.simulationRecordingPeriod) return

      // Build PW2 compact delta format
      val sb = new StringBuilder(1024 * 64)
      sb.append("PW2\n")
      sb.append("H|").append(mapHash).append('\n')
      sb.append("S|").append(startGameFrame).append('\n')
      // Unit metas
      metas.foreach { m =>
        sb.append("U|")
          .append(m.id).append('|')
          .append(if (m.friendly) '1' else '0').append('|')
          .append(Math.max(0, m.typeId)).append('\n')
      }

      // Diff state map
      case class St(var x: Int, var y: Int, var alive: Boolean, var hp: Int, var sh: Int, var tgt: Int, var cd: Int)
      val last = new java.util.HashMap[Int, St]()

      var flIdx = 0
      while (flIdx < framesLog.length) {
        val line = framesLog(flIdx)
        val parts = line.split("\\|")
        if (parts.length >= 2) {
          val fnum = try parts(0).toInt catch { case _: Exception => 0 }
          sb.append("F|").append(fnum).append('\n')
          val units = parts(1).split(';')
          val changes = new StringBuilder()
          var i = 0
          while (i < units.length) {
            val u = units(i)
            if (u.nonEmpty) {
              val f = u.split(',')
              if (f.length >= 14) {
                val id = try f(0).toInt catch { case _: Exception => -1 }
                if (id >= 0) {
                  val x = try f(2).toInt catch { case _: Exception => 0 }
                  val y = try f(3).toInt catch { case _: Exception => 0 }
                  val alive = try f(4).toBoolean catch { case _: Exception => true }
                  val hp = try f(7).toInt catch { case _: Exception => -1 }
                  val sh = try f(8).toInt catch { case _: Exception => -1 }
                  val tgt = try f(11).toInt catch { case _: Exception => -1 }
                  val cd  = try f(13).toInt catch { case _: Exception => -1 }
                  val prev = last.get(id)
                  val changed =
                    prev == null || prev.x != x || prev.y != y || prev.alive != alive || prev.hp != hp || prev.sh != sh || prev.tgt != tgt || prev.cd != cd
                  if (changed) {
                    if (changes.nonEmpty) changes.append(';')
                    changes
                      .append(id).append(',')
                      .append(x).append(',')
                      .append(y).append(',')
                      .append(if (alive) '1' else '0').append(',')
                      .append(hp).append(',')
                      .append(sh).append(',')
                      .append(tgt).append(',')
                      .append(cd)
                    if (prev == null) last.put(id, St(x, y, alive, hp, sh, tgt, cd))
                    else { prev.x = x; prev.y = y; prev.alive = alive; prev.hp = hp; prev.sh = sh; prev.tgt = tgt; prev.cd = cd }
                  }
                }
              }
            }
            i += 1
          }
          if (changes.nonEmpty) { sb.append("C|").append(changes.toString()).append('\n') }
          if (parts.length >= 3) {
            val tokens = parts(2).split(';')
            var ai = 0
            var aSB: StringBuilder = null
            var dSB: StringBuilder = null
            while (ai < tokens.length) {
              val t = tokens(ai)
              if (t.nonEmpty) {
                if (t.startsWith("a:")) {
                  val pv = t.substring(2)
                  val gt = pv.indexOf('>')
                  if (gt > 0) {
                    if (aSB == null) aSB = new StringBuilder()
                    else aSB.append(';')
                    aSB.append(pv)
                  }
                } else if (t.startsWith("d:")) {
                  val id = t.substring(2)
                  if (dSB == null) dSB = new StringBuilder() else dSB.append(';')
                  dSB.append(id)
                }
              }
              ai += 1
            }
            if (aSB != null && aSB.nonEmpty) sb.append("A|").append(aSB.toString()).append('\n')
            if (dSB != null && dSB.nonEmpty) sb.append("D|").append(dSB.toString()).append('\n')
          }
        }
        flIdx += 1
      }

      // Append PW2 block to opponent file with a completion marker
      sb.append("Z\n")
      val bytes = sb.toString().getBytes(StandardCharsets.UTF_8)
      val fos = new FileOutputStream(new File(outPath), true)
      try { fos.write(bytes) } finally { try fos.close() catch { case _: Throwable => } }
      // Diagnostics: increment counter and log
      try {
        val n = simAppendCounter.incrementAndGet()
        lastAppendStartFrame = startGameFrame
        lastAppendParticipantsKey = participantsKey
        lastPwcsAppendGameFrame = startGameFrame
      } catch { case _: Throwable => }
    } catch { case _: Throwable => }
  }

  // Async wrappers to avoid blocking main thread
  def writeSimulationLogAsync(battle: Battle, framesLog: ArrayBuffer[String], startGameFrame: Int): Unit = {
    try {
      // Capture all With.* dependent values on the main thread
      ensureDirs()
      if (gameStartEpochSeconds == 0L) gameStartEpochSeconds = System.currentTimeMillis() / 1000L
      val mapHash: String = try With.game.mapHash() catch { case _: Throwable => "" }
      val outPath: String = try opponentFile.getAbsolutePath catch { case _: Throwable => new File(With.bwapiData.write, With.enemy.name + ".pwcs").getAbsolutePath }
      val copy = new ArrayBuffer[String](framesLog.length)
      copy ++= framesLog
      enqueueIO(new Runnable { override def run(): Unit = writeSimulationLogPrepared(outPath, mapHash, copy, startGameFrame) })
    } catch { case _: Throwable => }
  }
  def writeCompressedSimDumpIfNeededAsync(battle: Battle, framesLog: ArrayBuffer[String], startGameFrame: Int): Unit = {
    try {
      // Throttle at enqueue time to prevent IO queue flooding
      val now = System.currentTimeMillis()
      val nextAllowed = nextDumpAllowedMs.get()
      if (now < nextAllowed) return
      if (!dumpScheduled.compareAndSet(false, true)) return
      nextDumpAllowedMs.set(now + 10000L)

      // Capture needed values on main thread
      ensureDirs()
      if (gameStartEpochSeconds == 0L) gameStartEpochSeconds = System.currentTimeMillis() / 1000L
      val basePath: String = try baseDir.getAbsolutePath catch { case _: Throwable => With.bwapiData.write }
      val oppSan: String = try With.enemy.name.replaceAll("[^A-Za-z0-9._-]", "_") catch { case _: Throwable => "opponent" }
      val curFrame: Int = try With.frame catch { case _: Throwable => 0 }
      val mapHash: String = try With.game.mapHash() catch { case _: Throwable => "" }
      val gsEpoch: Long = gameStartEpochSeconds
      val copy = new ArrayBuffer[String](framesLog.length)
      copy ++= framesLog
      enqueueDump(new Runnable { override def run(): Unit = {
        try {
          writeCompressedSimDumpIfNeededPrepared(basePath, oppSan, curFrame, mapHash, gsEpoch, copy, startGameFrame)
        } finally {
          dumpScheduled.set(false)
        }
      } })
    } catch { case _: Throwable => }
  }

  // Write a compressed .pwsim dump if conditions are met: at least one non-worker death and 10s since last dump
  // Prepared variant that avoids With.* and uses provided context
  private def writeCompressedSimDumpIfNeededPrepared(basePath: String, opponentSanitized: String, currentFrame: Int, mapHash: String, startEpochSeconds: Long, framesLog: ArrayBuffer[String], startGameFrame: Int): Unit = {
    try {
      // Enforce 10-second cooldown in game time (24 fps => 240 frames)
      if (currentFrame - lastDumpGameFrame < 240) return
      val now = System.currentTimeMillis()
      // Keep wall-clock throttle as a secondary guard
      if (now - lastDumpMs < 10000L) return
      val t0 = System.nanoTime()

      // Build unit metas with type IDs for worker filtering
      case class Meta(id: Int, friendly: Boolean, typeId: Int)
      val metasBuf = new scala.collection.mutable.ArrayBuffer[Meta]()
      framesLog.headOption.foreach { line =>
        val parts = line.split("\\|")
        if (parts.length >= 2) {
          val units = parts(1).split(';')
          var i = 0
          while (i < units.length) {
            val u = units(i)
            if (u.nonEmpty) {
              val f = u.split(',')
              if (f.length >= 14) {
                val id = try f(0).toInt catch { case _: Exception => -1 }
                val friendly = f(1).toBoolean
                val tpeName = try f(14) catch { case _: Exception => f.lastOption.getOrElse("") }
                val typeId = try bwapi.UnitType.valueOf(tpeName).id catch { case _: Throwable => -1 }
                if (id >= 0) metasBuf += Meta(id, friendly, typeId)
              }
            }
            i += 1
          }
        }
      }
      val metas = metasBuf.distinct.toVector
      val typeIdByUnitId: Map[Int, Int] = metas.map(m => m.id -> m.typeId).toMap

      // Check for at least one non-worker death across framesLog
      def isWorkerByTypeId(typeId: Int): Boolean = {
        if (typeId < 0) false
        else {
          val ut = bwapi.UnitType.values().find(_.id == typeId).orNull
          ut != null && ut.isWorker()
        }
      }
      var nonWorkerDeath = false
      var li = 0
      while (li < framesLog.length && !nonWorkerDeath) {
        val line = framesLog(li)
        val parts = line.split("\\|")
        if (parts.length >= 3) {
          val tokens = parts(2).split(';')
          var ti = 0
          while (ti < tokens.length && ! nonWorkerDeath) {
            val t = tokens(ti)
            if (t.nonEmpty && t.startsWith("d:")) {
              val idStr = t.substring(2)
              try {
                val id = idStr.toInt
                val typeId = typeIdByUnitId.getOrElse(id, -1)
                if (!isWorkerByTypeId(typeId)) nonWorkerDeath = true
              } catch { case _: Exception => }
            }
            ti += 1
          }
        }
        li += 1
      }
      if ( ! nonWorkerDeath) return

      // Build PW2 content
      val sb = new StringBuilder(1024 * 64)
      sb.append("PW2\n")
      sb.append("H|").append(mapHash).append('\n')
      sb.append("S|").append(startGameFrame).append('\n')
      metas.foreach { m =>
        sb.append("U|")
          .append(m.id).append('|')
          .append(if (m.friendly) '1' else '0').append('|')
          .append(Math.max(0, m.typeId)).append('\n')
      }
      case class St(var x: Int, var y: Int, var alive: Boolean, var hp: Int, var sh: Int, var tgt: Int, var cd: Int)
      val last = new java.util.HashMap[Int, St]()
      var fl = 0
      while (fl < framesLog.length) {
        val line = framesLog(fl)
        val parts = line.split("\\|")
        if (parts.length >= 2) {
          val fnum = try parts(0).toInt catch { case _: Exception => 0 }
          sb.append("F|").append(fnum).append('\n')
          val units = parts(1).split(';')
          val changes = new StringBuilder()
          var i = 0
          while (i < units.length) {
            val u = units(i)
            if (u.nonEmpty) {
              val f = u.split(',')
              if (f.length >= 14) {
                val id = try f(0).toInt catch { case _: Exception => -1 }
                if (id >= 0) {
                  val x = try f(2).toInt catch { case _: Exception => 0 }
                  val y = try f(3).toInt catch { case _: Exception => 0 }
                  val alive = try f(4).toBoolean catch { case _: Exception => true }
                  val hp = try f(7).toInt catch { case _: Exception => -1 }
                  val sh = try f(8).toInt catch { case _: Exception => -1 }
                  val tgt = try f(11).toInt catch { case _: Exception => -1 }
                  val cd  = try f(13).toInt catch { case _: Exception => -1 }
                  val prev = last.get(id)
                  val changed = prev == null || prev.x != x || prev.y != y || prev.alive != alive || prev.hp != hp || prev.sh != sh || prev.tgt != tgt || prev.cd != cd
                  if (changed) {
                    if (changes.nonEmpty) changes.append(';')
                    changes.append(id).append(',').append(x).append(',').append(y).append(',').append(if (alive) '1' else '0')
                      .append(',').append(hp).append(',').append(sh).append(',').append(tgt).append(',').append(cd)
                    if (prev == null) last.put(id, St(x, y, alive, hp, sh, tgt, cd))
                    else { prev.x = x; prev.y = y; prev.alive = alive; prev.hp = hp; prev.sh = sh; prev.tgt = tgt; prev.cd = cd }
                  }
                }
              }
            }
            i += 1
          }
          if (changes.nonEmpty) sb.append("C|").append(changes.toString()).append('\n')
          if (parts.length >= 3) {
            val tokens = parts(2).split(';')
            var aSB: StringBuilder = null
            var dSB: StringBuilder = null
            var ti = 0
            while (ti < tokens.length) {
              val t = tokens(ti)
              if (t.nonEmpty) {
                if (t.startsWith("a:")) {
                  val pv = t.substring(2)
                  val gt = pv.indexOf('>')
                  if (gt > 0) { if (aSB == null) aSB = new StringBuilder() else aSB.append(';'); aSB.append(pv) }
                } else if (t.startsWith("d:")) {
                  val id = t.substring(2)
                  if (dSB == null) dSB = new StringBuilder() else dSB.append(';')
                  dSB.append(id)
                }
              }
              ti += 1
            }
            if (aSB != null && aSB.nonEmpty) sb.append("A|").append(aSB.toString()).append('\n')
            if (dSB != null && dSB.nonEmpty) sb.append("D|").append(dSB.toString()).append('\n')
          }
        }
        fl += 1
      }

      // Filename in basePath
      val totalSec = Math.max(0, currentFrame / 24)
      val mm = (totalSec / 60) % 60
      val ss = totalSec % 60
      val name = f"$opponentSanitized-$startEpochSeconds-$mm%02d-$ss%02d.pwsim"
      var outFile = new File(basePath, name)
      var suffix = 1
      while (outFile.exists() && suffix < 1000) {
        val alt = f"$opponentSanitized-$startEpochSeconds-$mm%02d-$ss%02d-$suffix.pwsim"
        outFile = new File(basePath, alt)
        suffix += 1
      }
      val bytes = sb.toString().getBytes(StandardCharsets.UTF_8)
      val fos = new FileOutputStream(outFile)
      val gos = new java.util.zip.GZIPOutputStream(fos)
      try { gos.write(bytes) } finally { try gos.close() catch { case _: Throwable => }; try fos.close() catch { case _: Throwable => } }
      lastDumpMs = now
      lastDumpGameFrame = currentFrame
    } catch { case _: Throwable => }
  }

  def launchVisualizer(): Unit = {
    try {
      ensureDirs()
      val path = opponentFile.getAbsolutePath
      // Prefer launching an external Java process to isolate UI from the bot JVM
      val cp = System.getProperty("java.class.path", ".")
      val pb = new ProcessBuilder("java", "-cp", cp, "Debugging.PurpleSimViz", path)
      pb.directory(baseDir)
      pb.redirectErrorStream(true)
      visProcess = pb.start()
      launched = true
    } catch { case exception: Exception => With.logger.quietlyOnException(exception) }
  }

  def terminateVisualizer(): Unit = {
    try {
      if (launched) {
        if (visProcess != null) {
          try { visProcess.destroy() } catch { case _: Throwable => }
          visProcess = null
        } else {
          try Debugging.PurpleSimViz.requestClose() catch { case exception: Exception => With.logger.quietlyOnException(exception) }
        }
      }
    } catch { case exception: Exception => With.logger.quietlyOnException(exception) } finally { launched = false }
  }
}
