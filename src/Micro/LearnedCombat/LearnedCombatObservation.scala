package Micro.LearnedCombat

import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import mjson.Json

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import scala.collection.mutable

/** Versioned observations use only game-thread reads of PurpleWave's state. */
final class LearnedCombatObservation {
  import LearnedCombatObservation._

  private val seenFeatures = new mutable.HashMap[(Long, Long), Array[Float]]
  private var staticTerrain: Array[Float] = _
  private var lastFrame = 0
  private var lastCaptureTimings: Map[String, Double] = Map.empty
  private val unitFeatureMilestones = new Array[Long](5)
  def captureTimings: Map[String, Double] = lastCaptureTimings

  def initialize(): Unit = { staticTerrain = makeStaticTerrain() }
  private val decisions = new mutable.HashMap[(Long, Long), (Boolean, Int)]

  def recordDecision(unit: FriendlyUnitInfo): Unit = {
    val key = identity(unit)
    if (decisions.get(key).forall(_._1 != unit.agent.shouldFight))
      decisions.put(key, (unit.agent.shouldFight, With.frame))
  }

  private def bounded(value: Double, low: Double = -10.0, high: Double = 10.0): Float =
    if (java.lang.Double.isFinite(value)) Math.max(low, Math.min(high, value)).toFloat else 0.0f
  private def bit(value: Boolean): Float = if (value) 1.0f else 0.0f
  private def log(value: Double): Float = bounded(Math.log1p(Math.max(0, value)) / 10.0)
  private def px(value: Double): Float = bounded(value / Math.max(1, With.mapPixelWidth), 0, 1)
  private def py(value: Double): Float = bounded(value / Math.max(1, With.mapPixelHeight), 0, 1)
  private def walk(x: Int, y: Int): Float = bit(
    x >= 0 && y >= 0 && x < With.mapWalkWidth && y < With.mapWalkHeight && With.game.isWalkable(x, y))

  private def makeStaticTerrain(): Array[Float] = {
    val result = new Array[Float](TerrainPlanes * TerrainSize * TerrainSize)
    for (y <- 0 until TerrainSize; x <- 0 until TerrainSize) {
      val tx = Math.min(With.mapTileWidth - 1, x * With.mapTileWidth / TerrainSize)
      val ty = Math.min(With.mapTileHeight - 1, y * With.mapTileHeight / TerrainSize)
      val i = y * TerrainSize + x
      var walkable = 0.0f
      for (dy <- 0 until 4; dx <- 0 until 4) walkable += walk(tx * 4 + dx, ty * 4 + dy)
      result(i) = walkable / 16.0f
      result(4096 + i) = bounded(With.game.getGroundHeight(tx, ty) / 5.0)
      result(8192 + i) = bit(With.game.isBuildable(tx, ty))
    }
    result
  }

  private def unitFeatures(u: UnitInfo, neighbors: (Int, Int), elapsed: Int): Array[Float] = {
    unitFeatureMilestones(0) = System.nanoTime()
    val own = u.isOurs
    val observable = own || (u.visible && u.detected)
    val key = identity(u)
    val f = if (observable) new Array[Float](FeatureCount) else seenFeatures.get(key).map(_.clone()).getOrElse(new Array[Float](FeatureCount))
    val c = u.unitClass
    if (observable) {
      f(2) = bounded(u.velocityX / 16.0)
      f(3) = bounded(u.velocityY / 16.0)
      f(4) = bounded(Math.sin(u.angleRadians))
      f(5) = bounded(Math.cos(u.angleRadians))
      f(6) = bounded(u.hitPoints.toDouble / Math.max(1, c.maxHitPoints), 0, 2)
      f(7) = bounded(u.shieldPoints.toDouble / Math.max(1, c.maxShields), 0, 2)
      f(8) = bounded(u.matrixPoints / 250.0, 0, 2)
      f(9) = log(u.totalHealth)
      f(10) = log(c.maxHitPoints)
      f(11) = log(c.maxShields)
      f(12) = if (own) bounded(u.energy / 250.0) else 0.0f
      f(13) = bit(own)
      f(14) = if (own) bounded(u.cooldownGround / 120.0) else 0.0f
      f(15) = if (own) bounded(u.cooldownAir / 120.0) else 0.0f
      f(16) = if (own) bounded(u.cooldownSpell / 120.0) else 0.0f
      f(17) = bit(own)
      f(18) = bounded(u.pixelRangeGround / 512.0)
      f(19) = bounded(u.pixelRangeAir / 512.0)
      f(20) = bounded(u.pixelRangeMin / 512.0)
      f(21) = bounded(u.damageOnHitGround / 100.0)
      f(22) = bounded(u.damageOnHitAir / 100.0)
      f(23) = bounded(u.armorHealth / 10.0)
      f(24) = bounded(u.armorShield / 10.0)
      f(25) = bounded(u.topSpeed / 16.0)
      f(26) = bounded(c.width / 128.0)
      f(27) = bounded(c.height / 128.0)
      f(28) = bit(u.flying)
      f(29) = bit(u.complete)
      f(34) = bit(c.isWorker)
      f(35) = bit(c.isBuilding)
      f(36) = bit(c.attacksGround)
      f(37) = bit(c.attacksAir)
      f(38) = bit(c.isDetector)
      f(39) = bit(u.burrowed)
      f(40) = bit(u.cloaked)
      f(41) = bit(u.stimmed)
      f(42) = bit(u.ensnared)
      f(43) = bit(u.irradiated)
      f(44) = bit(u.plagued)
      f(45) = bit(u.lockedDown)
      f(46) = bit(u.maelstrommed)
      f(47) = bit(u.stasised)
      f(48) = bit(u.blind)
      f(49) = bit(u.invincible)
      f(50) = bit(u.underDarkSwarm)
      f(51) = bit(u.underDisruptionWeb)
      f(52) = bit(u.underStorm)
      f(53) = bit(u.powered)
      f(54) = bit(own && u.loaded)
      f(55) = bit(u.carryingMinerals)
      f(56) = bit(u.carryingGas)
      f(57) = bit(u.constructing)
      f(58) = bit(own && u.training)
      f(59) = bit(u.morphing)
      f(60) = bit(u.repairing)
      f(61) = if (own) bounded((u.scarabs + u.spiderMines + u.interceptors.size) / 10.0) else 0.0f
      f(62) = bit(own)
      f(63) = if (own) bounded(u.remainingCompletionFrames / 1440.0) else 0.0f
      f(64) = bit(u.canMove)
      f(82) = bounded((u.totalHealth - u.lastHitPoints - u.lastShieldPoints - u.lastMatrixPoints) / 100.0)
      f(83) = bounded(With.framesSince(u.lastFrameStartingAttack) / 240.0, 0, 10)
    }
    f(0) = px(u.pixel.x)
    f(1) = py(u.pixel.y)
    f(30) = bit(u.visible)
    f(31) = bit(u.detected)
    f(32) = bounded(With.framesSince(u.lastSeen) / 240.0, 0, 10)
    f(33) = bit(own || (u.visible && u.detected))
    unitFeatureMilestones(1) = System.nanoTime()
    unitFeatureMilestones(2) = unitFeatureMilestones(1)
    unitFeatureMilestones(3) = unitFeatureMilestones(1)
    // All private executor features are zero for enemy/remembered units.
    if (own) u.friendly.foreach(friendly => {
      val a = friendly.agent
      f(65) = bit(friendly.ready)
      f(66) = bit(friendly.intent.canFight)
      f(67) = bit(friendly.intent.canFlee)
      f(68) = bit(a.commit)
      f(69) = bit(a.shouldFight)
      f(70) = bounded(With.framesSince(decisions.get(key).map(_._2).getOrElse(With.frame)) / 24.0, 0, 10)
      val destination = a.destinationNext()
      f(71) = px(destination.x)
      f(72) = py(destination.y)
      val safety = a.safety
      f(73) = px(safety.x)
      f(74) = py(safety.y)
      unitFeatureMilestones(2) = System.nanoTime()
      a.toAttack.foreach(t => {
        f(75) = bounded((t.pixel.x - u.pixel.x) / 1024.0)
        f(76) = bounded((t.pixel.y - u.pixel.y) / 1024.0)
        f(77) = 1.0f
      })
      f(78) = bounded(friendly.matchups.threatNearest.map(u.pixelDistanceEdge).getOrElse(2048.0) / 1024.0)
      f(79) = bounded(friendly.matchups.targetNearest.map(u.pixelDistanceEdge).getOrElse(2048.0) / 1024.0)
      f(84) = bounded(With.framesSince(friendly.bwapiUnit.getLastCommandFrame) / 240.0, 0, 10)
      f(85) = bit(friendly.battle.isDefined)
      unitFeatureMilestones(3) = System.nanoTime()
    })
    f(80) = bounded(neighbors._1 / 64.0)
    f(81) = bounded(neighbors._2 / 64.0)
    val tx = Math.max(0, Math.min(With.mapTileWidth - 1, u.pixel.x / 32))
    val ty = Math.max(0, Math.min(With.mapTileHeight - 1, u.pixel.y / 32))
    f(86) = bounded(With.game.getGroundHeight(tx, ty) / 5.0)
    val directions = Array((0, -4), (4, -4), (4, 0), (4, 4), (0, 4), (-4, 4), (-4, 0), (-4, -4))
    directions.indices.foreach(i => f(87 + i) = walk(u.pixel.x / 8 + directions(i)._1, u.pixel.y / 8 + directions(i)._2))
    f(95) = bounded(elapsed / 24.0, 0, 10)
    if (observable) seenFeatures.put(key, f.clone())
    unitFeatureMilestones(4) = System.nanoTime()
    f
  }

  def capture(sequence: Long, audits: Vector[String]): Array[Byte] = {
    val captureStart = System.nanoTime()
    val elapsed = Math.max(0, With.frame - lastFrame)
    lastFrame = With.frame
    val all = (With.units.ours.toVector ++ With.units.enemy.toVector).filter(_.alive).sortBy(_.id)
    if (all.size > With.configuration.learnedCombatMaxEntities || all.size > 2048)
      throw new IllegalStateException("Learned combat entity limit exceeded; whole observation falls back")
    val currentIds = all.map(identity).toSet
    decisions.keys.filterNot(currentIds.contains).toVector.foreach(decisions.remove)
    seenFeatures.keys.filterNot(currentIds).toVector.foreach(seenFeatures.remove)
    if (staticTerrain == null) staticTerrain = makeStaticTerrain()
    val setupEnd = System.nanoTime()
    val terrain = staticTerrain.clone()
    for (y <- 0 until TerrainSize; x <- 0 until TerrainSize) {
      val tx = Math.min(With.mapTileWidth - 1, x * With.mapTileWidth / TerrainSize)
      val ty = Math.min(With.mapTileHeight - 1, y * With.mapTileHeight / TerrainSize)
      val i = y * TerrainSize + x
      terrain(12288 + i) = bit(With.game.isExplored(tx, ty))
      val visible = With.game.isVisible(tx, ty)
      terrain(16384 + i) = bit(visible)
      terrain(20480 + i) = bit(visible && With.game.hasCreep(tx, ty))
    }
    all.filter(u => u.isOurs || u.visible).foreach(u => {
      val x = Math.max(0, Math.min(63, (64L * u.pixel.x / Math.max(1, With.mapPixelWidth)).toInt))
      val y = Math.max(0, Math.min(63, (64L * u.pixel.y / Math.max(1, With.mapPixelHeight)).toInt))
      val i = (if (u.isOurs) 24576 else 28672) + y * 64 + x
      terrain(i) = Math.min(1.0f, terrain(i) + 0.125f)
    })
    val terrainEnd = System.nanoTime()
    val own = all.filter(_.isOurs)
    val visible = all.filter(u => u.isEnemy && u.visible)
    val remembered = all.filter(u => u.isEnemy && !u.visible)
    val p = With.self.bwapiPlayer
    val global = Array[Float](
      bounded(With.frame / 86400.0), bounded(elapsed / 24.0), bounded(With.mapTileWidth / 256.0), bounded(With.mapTileHeight / 256.0),
      log(p.minerals()), log(p.gas()), bounded(p.supplyUsed() / 400.0), bounded(p.supplyTotal() / 400.0),
      bit(With.self.isProtoss), bit(With.self.isTerran), bit(With.self.isZerg),
      bit(With.enemies.exists(_.isProtoss)), bit(With.enemies.exists(_.isTerran)), bit(With.enemies.exists(_.isZerg)),
      bounded(With.latency.latencyFrames / 24.0), bounded(With.framesSince(With.agents.lastQueueCompletion) / 24.0),
      bit(With.blackboard.wantToAttack()), bounded(own.count(_.unitClass.isWorker) / 100.0),
      bounded(own.count(_.unitClass.isBuilding) / 100.0), bounded(own.count(_.canAttack) / 200.0), log(own.map(_.totalHealth).sum),
      bounded(visible.count(_.unitClass.isWorker) / 100.0), bounded(visible.count(_.unitClass.isBuilding) / 100.0),
      bounded(visible.count(_.canAttack) / 200.0), log(visible.map(_.totalHealth).sum), bounded(remembered.size / 200.0),
      log(remembered.map(_.totalHealth).sum), log(p.getUnitScore + p.getKillScore), 0.0f, 0.0f, log(p.spentMinerals()), log(p.spentGas()))
    val globalsEnd = System.nanoTime()
    val neighbors = LearnedCombatNeighbors.counts(all.map(u => LearnedCombatNeighbors.Point(u.pixel.x, u.pixel.y, u.player.id)))
    val neighborsEnd = System.nanoTime()
    var slowestUnitMs = 0.0
    var slowestUnitId = -1
    var slowestUnitPhases = Map.empty[String, Double]
    val features = all.indices.map(i => {
      val u = all(i)
      val unitStart = System.nanoTime()
      val row = unitFeatures(u, neighbors(i), elapsed)
      val unitMs = (System.nanoTime() - unitStart) / 1000000.0
      if (unitMs > slowestUnitMs) {
        slowestUnitMs = unitMs
        slowestUnitId = u.id
        slowestUnitPhases = Map(
          "slowest_unit_attributes_ms" -> ((unitFeatureMilestones(1) - unitFeatureMilestones(0)) / 1000000.0),
          "slowest_unit_agent_state_ms" -> ((unitFeatureMilestones(2) - unitFeatureMilestones(1)) / 1000000.0),
          "slowest_unit_targeting_ms" -> ((unitFeatureMilestones(3) - unitFeatureMilestones(2)) / 1000000.0),
          "slowest_unit_terrain_ms" -> ((unitFeatureMilestones(4) - unitFeatureMilestones(3)) / 1000000.0))
      }
      row
    })
    val featuresEnd = System.nanoTime()
    val auditBytes = ("[" + audits.mkString(",") + "]").getBytes(StandardCharsets.UTF_8)
    val out = ByteBuffer.allocate(20 + all.size * (25 + FeatureCount * 4) + global.length * 4 + terrain.length * 4 + 4 + auditBytes.length)
    def putFloats(values: Array[Float]): Unit = {
      out.asFloatBuffer().put(values)
      out.position(out.position() + values.length * 4)
    }
    out.putLong(sequence)
    out.putInt(With.frame)
    out.putInt(elapsed)
    out.putInt(all.size)
    all.foreach(u => { val key = identity(u); out.putLong(key._1); out.putLong(key._2) })
    all.foreach(u => out.putInt(Math.max(0, Math.min(255, u.unitClass.bwapiType.id))))
    all.foreach(u => out.putInt(if (u.isOurs) 0 else 1))
    all.foreach(u => out.put((if (u.isOurs && u.canMove && u.complete && u.battle.isDefined) 1 else 0).toByte))
    features.foreach(putFloats)
    putFloats(global)
    putFloats(terrain)
    out.putInt(auditBytes.length)
    out.put(auditBytes)
    require(!out.hasRemaining, "observation buffer length mismatch")
    lastCaptureTimings = Map(
      "setup_ms" -> ((setupEnd - captureStart) / 1000000.0),
      "terrain_ms" -> ((terrainEnd - setupEnd) / 1000000.0),
      "globals_ms" -> ((globalsEnd - terrainEnd) / 1000000.0),
      "features_ms" -> ((featuresEnd - globalsEnd) / 1000000.0),
      "neighbors_ms" -> ((neighborsEnd - globalsEnd) / 1000000.0),
      "encoding_ms" -> ((System.nanoTime() - featuresEnd) / 1000000.0),
      "slowest_unit_ms" -> slowestUnitMs, "slowest_unit_id" -> slowestUnitId.toDouble) ++ slowestUnitPhases
    out.array()
  }
}

object LearnedCombatObservation {
  val SchemaHash = "6dc13ed6361b4e3b249e811c906c141820d948ee40d2899abfe5aead6efcf0c1"
  val FeatureCount = 96
  val TerrainSize = 64
  val TerrainPlanes = 8
  def identity(unit: UnitInfo): (Long, Long) = (unit.id.toLong, unit.frameDiscovered.toLong * 16 + unit.player.id)
}
