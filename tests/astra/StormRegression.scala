package Regression

import Debugging.Logger
import Information.Geography.Geography
import Information.Grids.Grids
import Lifecycle.Configure.Configuration
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Micro.Actions.Action
import Micro.Agency.Agency
import Micro.Coordination.Coordinator
import Micro.Coordination.Pushing.{CircularPush, TrafficPriorities}
import ProxyBwapi.Bullets.Bullets
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import ProxyBwapi.UnitTracking.{UnitTracker, Visibility}
import bwapi.{HeadlessBWAPI, Race, UnitCommandType}

import scala.collection.mutable.ArrayBuffer

object StormRegression extends App {
  var passed = 0
  val failed = ArrayBuffer.empty[String]
  def check(name: String)(body: => Unit): Unit = {
    try { body; passed += 1; println("PASS " + name) }
    catch { case error: Throwable => failed += name; println("FAIL " + name); error.printStackTrace() }
  }
  def set(target: AnyRef, name: String, value: Any): Unit = {
    val field = Iterator.iterate[Class[_]](target.getClass)(_.getSuperclass).takeWhile(_ != null)
      .flatMap(_.getDeclaredFields).find(f => f.getName == name || f.getName.endsWith("$$" + name)).get
    field.setAccessible(true)
    field.set(target, value.asInstanceOf[AnyRef])
  }

  With.configuration = new Configuration
  With.logger = new Logger
  With.performance = new Performance.PerformanceMonitor
  With.frame = 1000
  With.mapTileWidth = 32
  With.mapTileHeight = 32
  With.mapTileArea = 1024
  With.mapPixelWidth = 1024
  With.mapPixelHeight = 1024
  With.mapWalkWidth = 128
  With.mapWalkHeight = 128
  With.mapWalkArea = 16384
  val us = PlayerInfo(HeadlessBWAPI.player(0, Race.Protoss))
  With.self = us
  With.friendlies = Vector(us)
  With.enemies = Vector.empty
  With.game = HeadlessBWAPI.game(us.bwapiPlayer)
  With.proxy = new ProxyBwapi.ProxyBWAPI
  With.grids = new Grids
  With.grids.walkableTerrain.indices.foreach(i => With.grids.walkableTerrain.set(i, true))
  With.units = new UnitTracker
  With.battles = new Information.Battles.Battles
  With.matchups = new Micro.Matchups.MatchupGraph
  With.geography = new Geography
  set(With.geography, "_zoneByTile", new Array[Information.Geography.Types.Zone](With.mapTileArea))
  With.paths = new Information.Geography.Pathfinding.Paths
  With.agents = new Agency
  With.latency = new Performance.Latency
  With.reaction = new Performance.ReactionTimes { override def sluggishness: Int = 3 }
  With.coordinator = new Coordinator
  With.bullets = new Bullets
  var nextId = 0
  def unit(kind: UnitClass, pixel: Pixel): FriendlyUnitInfo = {
    nextId += 1
    val result = new FriendlyUnitInfo(HeadlessBWAPI.unit(With.game, nextId, kind.base, pixel.x, pixel.y), nextId)
    Seq[(String, Any)](
      "_player" -> us, "_unitClass" -> kind, "_pixel" -> pixel, "_pixelObserved" -> pixel,
      "_tile" -> pixel.tile, "_visibility" -> Visibility.Visible, "_alive" -> true, "_complete" -> true,
      "_powered" -> true, "_detected" -> true, "_flying" -> kind.isFlyer,
      "_hitPoints" -> kind.maxHitPoints, "_shieldPoints" -> kind.maxShields).foreach { case (name, value) => set(result, name, value) }
    result.completionFrame = 0
    result
  }
  def storm(center: Pixel): Unit = {
    With.frame += 1
    With.coordinator = new Coordinator
    HeadlessBWAPI.storm(With.game, center.x, center.y)
    With.coordinator.pushes.addExplosions()
  }

  check("storm bullet lookup includes the 33-pixel tile-boundary regression") {
    storm(Pixel(351, 351))
    val observer = unit(Protoss.Observer, Pixel(384, 352))
    assert(observer.pixel.pixelDistance(Pixel(351, 351)) < 64)
    assert(observer.agent.receivedPushPriority() == TrafficPriorities.Dodge)
    assert(observer.agent.receivedPushForce().lengthSquared > 0)
  }
  check("circular push lookup covers every tile alignment and fractional radius") {
    for (radius <- Seq(1.0, 31.5, 32.0, 64.0, 96.0); ox <- 0 until 32; oy <- 0 until 32) {
      val center = Pixel(320 + ox, 320 + oy)
      val push = new CircularPush(TrafficPriorities.Dodge, center, radius)
      val tiles = push.tiles.toSet
      val reach = math.ceil(radius / 32).toInt + 1
      for (dx <- -reach to reach; dy <- -reach to reach) {
        val tile = center.tile.add(dx, dy)
        val nearest = Pixel(math.max(tile.x * 32, math.min(tile.x * 32 + 31, center.x)),
          math.max(tile.y * 32, math.min(tile.y * 32 + 31, center.y)))
        if (center.pixelDistance(nearest) < radius) {
          assert(tiles.contains(tile), s"radius=$radius offset=($ox,$oy) missing $tile")
        }
      }
    }
  }
  check("overinclusive tile lookup does not exert force outside the storm") {
    val push = new CircularPush(TrafficPriorities.Dodge, Pixel(351, 351), 64)
    assert(push.force(unit(Protoss.Observer, Pixel(400, 400))).isEmpty)
    assert(push.force(unit(Protoss.Observer, Pixel(415, 351))).isEmpty)
    assert(push.force(unit(Protoss.Pylon, Pixel(384, 352))).nonEmpty) // CircularPush itself is generic.
    storm(Pixel(351, 351))
    assert(unit(Protoss.Pylon, Pixel(384, 352)).agent.receivedPushPriority() == TrafficPriorities.Freedom)
  }
  check("storm dodge precedes specialized, worker and scout intentions") {
    for (kind <- Seq(Protoss.Observer, Protoss.Probe, Protoss.Dragoon)) {
      storm(Pixel(320, 320))
      val dodger = unit(kind, Pixel(336, 320))
      var ordinaryActions = 0
      dodger.intent.action = new Action {
        override protected def perform(unit: FriendlyUnitInfo): Unit = { ordinaryActions += 1 }
      }
      dodger.agent.home.set(Pixel(100, 100))
      dodger.agent.execute()
      assert(ordinaryActions == 0, s"$kind ran its ordinary action inside a storm")
      val command = HeadlessBWAPI.command(dodger.bwapiUnit)
      assert(command != null && command.getType == UnitCommandType.Move)
      assert(command.getTargetPosition.getX > dodger.pixel.x)
      assert(dodger.agent.actions.contains("Dodge"))
    }
  }
  check("a sluggish combat dodge issues Move rather than Attack_Move") {
    storm(Pixel(320, 320))
    val dodger = unit(Protoss.Corsair, Pixel(336, 320))
    dodger.agent.shouldFight = true
    val method = dodger.agent.combat.getClass.getDeclaredMethod("dodge")
    method.setAccessible(true)
    method.invoke(dodger.agent.combat)
    val command = HeadlessBWAPI.command(dodger.bwapiUnit)
    assert(command != null && command.getType == UnitCommandType.Move)
    assert(!dodger.agent.shouldFight)
  }
  check("ordinary intentions still run when outside the storm") {
    storm(Pixel(320, 320))
    val observer = unit(Protoss.Observer, Pixel(700, 700))
    var ordinaryActions = 0
    observer.intent.action = new Action {
      override protected def perform(unit: FriendlyUnitInfo): Unit = { ordinaryActions += 1 }
    }
    observer.agent.execute()
    assert(ordinaryActions == 1 && HeadlessBWAPI.command(observer.bwapiUnit) == null)
  }
  check("storm priority does not bypass command sleep") {
    storm(Pixel(320, 320))
    val observer = unit(Protoss.Observer, Pixel(336, 320))
    observer.sleepUntil = With.frame + 5
    observer.agent.execute()
    assert(HeadlessBWAPI.command(observer.bwapiUnit) == null)
  }
  check("loaded passengers retain their ordinary transport behavior") {
    storm(Pixel(320, 320))
    val passenger = unit(Protoss.Dragoon, Pixel(336, 320))
    set(passenger, "_loaded", true)
    var ordinaryActions = 0
    passenger.intent.action = new Action {
      override protected def perform(unit: FriendlyUnitInfo): Unit = { ordinaryActions += 1 }
    }
    passenger.agent.execute()
    assert(ordinaryActions == 1 && HeadlessBWAPI.command(passenger.bwapiUnit) == null)
  }
  println(s"$passed storm checks passed; ${failed.size} failed")
  assert(failed.isEmpty, failed.mkString(", "))
}
