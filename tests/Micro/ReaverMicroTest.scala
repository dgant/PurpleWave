package Micro

import Information.Geography.Geography
import Information.Geography.Pathfinding.Paths
import Information.Geography.Types.{Edge, Zone}
import Information.Grids.Grids
import Information.Grids.Movement.{GridGroundDistance, GridUnwalkableUnits, GridWalkable, GridWalkableTerrain}
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Performance.{Cache, Latency, ReactionTimes}
import ProxyBwapi.ProxyBWAPI
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import ProxyBwapi.UnitTracking.{ScarabTracker, UnitTracker}

object ReaverMicroTest extends App {
  // Synthetic observations exercise the production tracker, readiness and terrain-distance checks.
  val unsafeField = classOf[sun.misc.Unsafe].getDeclaredField("theUnsafe")
  unsafeField.setAccessible(true)
  val unsafe = unsafeField.get(null).asInstanceOf[sun.misc.Unsafe]
  def allocate[T](clazz: Class[T]): T = unsafe.allocateInstance(clazz).asInstanceOf[T]
  def set(obj: AnyRef, name: String, value: Any): Unit = {
    val fields = Iterator.iterate[Class[_]](obj.getClass)(_.getSuperclass).takeWhile(_ != null).flatMap(_.getDeclaredFields)
    val field = fields.find(f => f.getName == name || f.getName.endsWith("$$" + name)).get
    field.setAccessible(true)
    field.set(obj, value.asInstanceOf[AnyRef])
  }
  var passed = 0
  def check(name: String)(body: => Unit): Unit = { body; passed += 1; println("PASS " + name) }

  With.frame = 100
  With.proxy = allocate(classOf[ProxyBWAPI])
  set(With.proxy, "unitClasses", UnitTypes.all.map(UnitClass))
  With.units = new UnitTracker
  With.latency = allocate(classOf[Latency])
  With.reaction = new ReactionTimes { override def agencyMin: Int = 0 }

  def unit(id: Int, kind: UnitClass, x: Int, cooldown: Int = 0): FriendlyUnitInfo = {
    val u = allocate(classOf[FriendlyUnitInfo])
    set(u, "id", id)
    set(u, "_unitClass", kind)
    set(u, "_pixel", Pixel(x, 64))
    set(u, "_alive", true)
    set(u, "_cooldownRaw", cooldown)
    set(u, "frameDiscovered", With.frame)
    set(u, "canAttackCache", new Cache(() => true))
    set(u, "_cooldownLeft", new Cache(() => u.cooldownRaw))
    set(u, "pixelRangeGroundCache", new Cache(() => 256.0))
    u.lastFrameStartingAttack = With.frame
    u
  }

  check("an active Scarab blocks a zero-cooldown Reaver until the Scarab dies") {
    val reaver = unit(398, Protoss.Reaver, 100, 60)
    val scarab = unit(900, Protoss.Scarab, 125)
    With.units.scarabs.update(Vector(reaver, scarab))
    assert(reaver.hasActiveScarab)
    With.frame += 60
    set(reaver, "_cooldownRaw", 0)
    set(scarab, "_pixel", Pixel(600, 64))
    With.units.scarabs.update(Vector(reaver, scarab))
    assert(!reaver.readyForAttackOrder)
    set(scarab, "_alive", false)
    With.units.scarabs.update(Vector(reaver))
    assert(reaver.readyForAttackOrder)
  }
  check("other Reavers and non-Reavers retain normal readiness") {
    assert(unit(1, Protoss.Reaver, 100).readyForAttackOrder)
    assert(unit(2, Protoss.Dragoon, 100).readyForAttackOrder)
    assert(!unit(3, Protoss.Reaver, 100, 30).readyForAttackOrder)
  }
  check("a landing cooldown cannot claim a nearby Scarab") {
    val tracker = new ScarabTracker
    val reaver = unit(1, Protoss.Reaver, 100, 30)
    tracker.update(Vector(reaver, unit(9, Protoss.Scarab, 125)))
    assert(tracker.launchedBy(reaver).isEmpty)
  }
  check("loaded, distant and stale-firing Reavers cannot claim a launch") {
    val tracker = new ScarabTracker
    val loaded = unit(1, Protoss.Reaver, 100, 60)
    set(loaded, "_loaded", true)
    val distant = unit(2, Protoss.Reaver, 300, 60)
    val stale = unit(3, Protoss.Reaver, 100, 59)
    stale.lastFrameStartingAttack -= 1
    tracker.update(Vector(loaded, distant, stale, unit(9, Protoss.Scarab, 125)))
    assert(Vector(loaded, distant, stale).forall(tracker.launchedBy(_).isEmpty))
  }
  check("ambiguous simultaneous launches do not block either possible parent") {
    val tracker = new ScarabTracker
    val a = unit(1, Protoss.Reaver, 100, 60)
    val b = unit(2, Protoss.Reaver, 110, 60)
    tracker.update(Vector(a, b, unit(9, Protoss.Scarab, 125)))
    assert(tracker.launchedBy(a).isEmpty && tracker.launchedBy(b).isEmpty)
  }
  check("separated simultaneous launches retain independent identities") {
    val tracker = new ScarabTracker
    val a = unit(1, Protoss.Reaver, 100, 60)
    val b = unit(2, Protoss.Reaver, 300, 60)
    val sa = unit(8, Protoss.Scarab, 125)
    val sb = unit(9, Protoss.Scarab, 325)
    tracker.update(Vector(a, b, sa, sb))
    assert(tracker.launchedBy(a).contains(sa) && tracker.launchedBy(b).contains(sb))
    set(sa, "_alive", false)
    tracker.update(Vector(a, b, sb))
    assert(tracker.launchedBy(a).isEmpty && tracker.launchedBy(b).contains(sb))
  }

  With.mapTileWidth = 32
  With.mapTileHeight = 32
  With.mapTileArea = 1024
  With.mapPixelWidth = 1024
  With.mapPixelHeight = 1024
  With.grids = allocate(classOf[Grids])
  set(With.grids, "walkable", new GridWalkable)
  set(With.grids, "walkableTerrain", new GridWalkableTerrain)
  set(With.grids, "unwalkableUnits", new GridUnwalkableUnits)
  java.util.Arrays.fill(With.grids.walkableTerrain.rawValues, true)
  With.geography = allocate(classOf[Geography])
  With.paths = allocate(classOf[Paths])
  set(With.paths, "impossiblyLargeDistanceTiles", 256 * 256)
  val originZone = allocate(classOf[Zone])
  val targetZone = allocate(classOf[Zone])
  set(targetZone, "bitmap$0", -1L)
  val zones = Array.fill(With.mapTileArea)(originZone)
  val from = Tile(1, 1).center
  val target = unit(10, Protoss.Assimilator, 240)
  set(target, "_pixel", Tile(7, 1).center)
  set(target, "_tile", Tile(7, 1))
  zones(target.tile.i) = targetZone
  set(With.geography, "_zoneByTile", zones)
  val edge = allocate(classOf[Edge])
  set(edge, "distanceGrid", new GridGroundDistance())
  set(targetZone, "edges", Vector(edge))
  val reaver = unit(11, Protoss.Reaver, 48)

  check("nearby targets across a long ground route cannot authorize a combat drop") {
    edge.distanceGrid.set(from.tile, 12)
    edge.distanceGrid.set(target.tile, 12)
    assert(from.pixelDistance(target.pixel) < 256)
    assert(!reaver.hasScarabPathTo(target, from))
  }
  check("the firing-path limit is strict and reachable short routes remain legal") {
    edge.distanceGrid.set(from.tile, 8)
    edge.distanceGrid.set(target.tile, 8)
    assert(!reaver.hasScarabPathTo(target, from))
    edge.distanceGrid.set(target.tile, 7)
    assert(reaver.hasScarabPathTo(target, from))
  }
  check("an unreachable target cannot authorize a combat drop") {
    set(targetZone, "edges", Vector.empty[Edge])
    assert(!reaver.hasScarabPathTo(target, from))
  }
  println(s"$passed Reaver micro checks passed")
}
