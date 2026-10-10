package Micro

import Information.Geography.Pathfinding.{BroodWarGroundDistance, Paths}
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Performance.{Cache, Latency, ReactionTimes}
import ProxyBwapi.ProxyBWAPI
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import ProxyBwapi.UnitTracking.{ScarabTracker, UnitTracker}

import scala.collection.JavaConverters._

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

  def discover(tracker: ScarabTracker, units: Vector[FriendlyUnitInfo]): Unit =
    units.filter(u => Protoss.Scarab(u) && u.frameDiscovered == With.frame).foreach(tracker.onUnitCreate(_, units))

  check("an active Scarab blocks a zero-cooldown Reaver until the Scarab dies") {
    val reaver = unit(398, Protoss.Reaver, 100, 60)
    val scarab = unit(900, Protoss.Scarab, 125)
    discover(With.units.scarabs, Vector(reaver, scarab))
    assert(reaver.hasActiveScarab)
    With.frame += 60
    set(reaver, "_cooldownRaw", 0)
    set(scarab, "_pixel", Pixel(600, 64))
    discover(With.units.scarabs, Vector(reaver, scarab))
    assert(!reaver.readyForAttackOrder)
    set(scarab, "_alive", false)
    discover(With.units.scarabs, Vector(reaver))
    assert(reaver.readyForAttackOrder)
  }
  check("other Reavers and non-Reavers retain normal readiness") {
    assert(unit(1, Protoss.Reaver, 100).readyForAttackOrder)
    assert(unit(2, Protoss.Dragoon, 100).readyForAttackOrder)
    assert(!unit(3, Protoss.Reaver, 100, 30).readyForAttackOrder)
  }
  check("attack orders include latency, reaction and a conservative firing turn") {
    set(With.latency, "_remainingFrames", 3)
    With.reaction = new ReactionTimes { override def agencyMin: Int = 2 }
    assert(Protoss.Reaver.framesToTurn180 == 7)
    assert(unit(20, Protoss.Reaver, 100, 12).readyForAttackOrder)
    assert(!unit(21, Protoss.Reaver, 100, 13).readyForAttackOrder)
    assert(unit(22, Protoss.Dragoon, 100, 5).readyForAttackOrder)
    assert(!unit(23, Protoss.Dragoon, 100, 6).readyForAttackOrder)
    set(With.latency, "_remainingFrames", 0)
    With.reaction = new ReactionTimes { override def agencyMin: Int = 0 }
  }
  check("units with independent turrets do not wait for a body turn") {
    assert(ProxyBwapi.Races.Terran.Goliath.framesToTurn180 > 0)
    assert(!unit(24, ProxyBwapi.Races.Terran.Goliath, 100, 1).readyForAttackOrder)
  }
  check("a landing cooldown cannot claim a nearby Scarab") {
    val tracker = new ScarabTracker
    val reaver = unit(1, Protoss.Reaver, 100, 30)
    discover(tracker, Vector(reaver, unit(9, Protoss.Scarab, 125)))
    assert(tracker.launchedBy(reaver).isEmpty)
  }
  check("loaded, distant and stale-firing Reavers cannot claim a launch") {
    val tracker = new ScarabTracker
    val loaded = unit(1, Protoss.Reaver, 100, 60)
    set(loaded, "_loaded", true)
    val distant = unit(2, Protoss.Reaver, 300, 60)
    val stale = unit(3, Protoss.Reaver, 100, 59)
    stale.lastFrameStartingAttack -= 1
    discover(tracker, Vector(loaded, distant, stale, unit(9, Protoss.Scarab, 125)))
    assert(Vector(loaded, distant, stale).forall(tracker.launchedBy(_).isEmpty))
  }
  check("ambiguous simultaneous launches do not block either possible parent") {
    val tracker = new ScarabTracker
    val a = unit(1, Protoss.Reaver, 100, 60)
    val b = unit(2, Protoss.Reaver, 110, 60)
    discover(tracker, Vector(a, b, unit(9, Protoss.Scarab, 125)))
    assert(tracker.launchedBy(a).isEmpty && tracker.launchedBy(b).isEmpty)
  }
  check("separated simultaneous launches retain independent identities") {
    val tracker = new ScarabTracker
    val a = unit(1, Protoss.Reaver, 100, 60)
    val b = unit(2, Protoss.Reaver, 300, 60)
    val sa = unit(8, Protoss.Scarab, 125)
    val sb = unit(9, Protoss.Scarab, 325)
    discover(tracker, Vector(a, b, sa, sb))
    assert(tracker.launchedBy(a).contains(sa) && tracker.launchedBy(b).contains(sb))
    set(sa, "_alive", false)
    discover(tracker, Vector(a, b, sb))
    assert(tracker.launchedBy(a).isEmpty && tracker.launchedBy(b).contains(sb))
  }

  With.mapTileWidth = 32
  With.mapTileHeight = 32
  With.mapTileArea = 1024
  With.mapPixelWidth = 1024
  With.mapPixelHeight = 1024
  val from = Tile(1, 1).center
  val target = unit(10, Protoss.Assimilator, 240)
  set(target, "_pixel", Tile(7, 1).center)
  val reaver = unit(11, Protoss.Reaver, 48)

  // Feed the actual BWAPI adapter, including getRegionAt, with three native regions.
  def nativeMap(centers: Vector[Pixel], groups: Vector[Int] = Vector(1, 1, 1)): Unit = {
    val regions = centers.indices.map(i => {
      val r = allocate(classOf[bwapi.Region])
      set(r, "id", i)
      set(r, "center", centers(i).bwapi)
      set(r, "regionGroupID", groups(i))
      set(r, "accessible", true)
      r
    }).toArray
    regions.indices.foreach(i => set(regions(i), "neighbours", regions.indices.filter(j => Math.abs(i - j) == 1).map(regions).asJava))
    With.game = allocate(classOf[bwapi.Game])
    set(With.game, "mapWidth", 32)
    set(With.game, "mapHeight", 32)
    set(With.game, "mapPixelWidth", 1024)
    set(With.game, "mapPixelHeight", 1024)
    set(With.game, "regions", regions)
    set(With.game, "regionSet", regions.toVector.asJava)
    val tiles = Array.ofDim[Short](32, 32)
    tiles(7)(1) = 2
    set(With.game, "mapTileRegionID", tiles)
    With.paths = new Paths
  }
  check("native regions reject a nearby target across a long ground route") {
    nativeMap(Vector(Pixel(48, 48), Pixel(48, 432), Pixel(240, 48)))
    assert(from.pixelDistance(target.pixel) < 256)
    assert(!reaver.hasScarabPathTo(target, from))
  }
  check("the native firing-path limit is strict and shorter routes stay legal") {
    nativeMap(Vector(Pixel(48, 48), Pixel(304, 48), Pixel(560, 48)))
    assert(With.paths.broodWarGroundDistance.pixels(from, target.pixel) == 512)
    assert(!reaver.hasScarabPathTo(target, from))
    nativeMap(Vector(Pixel(48, 48), Pixel(304, 48), Pixel(528, 48)))
    assert(reaver.hasScarabPathTo(target, from))
  }
  check("different native region groups cannot authorize a combat drop") {
    nativeMap(Vector(Pixel(48, 48), Pixel(304, 48), Pixel(528, 48)), Vector(1, 1, 2))
    assert(!reaver.hasScarabPathTo(target, from))
  }
  import BroodWarGroundDistance.Region
  check("one- and two-region routes measure endpoints, not region centers") {
    val graph = Array(Region(Pixel(0, 0), 1, true, Vector(1)), Region(Pixel(1000, 0), 1, true, Vector(0)))
    val distance = new BroodWarGroundDistance(graph, p => if (p.x < 200) 0 else 1)
    assert(distance.pixels(Pixel(0, 0), Pixel(100, 25)) == 100) // BW's quarter-slope boundary.
    assert(distance.pixels(Pixel(190, 0), Pixel(210, 0)) == 20)
  }
  check("endpoint changes within a region can choose a different route") {
    val graph = Array(
      Region(Pixel(0, 0), 1, true, Vector(1, 2)),
      Region(Pixel(0, 100), 1, true, Vector(0, 3)),
      Region(Pixel(800, 200), 1, true, Vector(0, 3)),
      Region(Pixel(1000, 0), 1, true, Vector(1, 2)))
    val to = Pixel(1000, 0)
    val distance = new BroodWarGroundDistance(graph, p => if (p == to) 3 else 0)
    assert(distance.pixels(Pixel(0, 90), to) == 1100)
    assert(distance.pixels(Pixel(800, 190), to) == 1062)
    assert(distance.pixels(Pixel(0, 90), to) == 1100)
  }
  check("missing regions fail closed and bounded searches match BW's 50-region prefix") {
    val graph = (0 to 350).map(i => Region(Pixel(i * 2, 0), 1, true, Vector(i - 1, i + 1).filter(j => j >= 0 && j <= 350))).toArray
    val distance = new BroodWarGroundDistance(graph, p => p.x / 2)
    assert(distance.pixels(Pixel(0, 0), Pixel(700, 0)) == 98)
    assert(distance.pixels(Pixel(0, 0), Pixel(702, 0)) == BroodWarGroundDistance.unreachable)
    assert(distance.pixels(Pixel(-1, 0), Pixel(0, 0)) == BroodWarGroundDistance.unreachable)
  }
  import Micro.Actions.Protoss.ReaverShot
  check("shot preparation yields to ordinary combat during the cooldown movement interval") {
    val previousReaction = With.reaction
    With.reaction = new ReactionTimes { override def agencyMax: Int = 1 }
    val r = unit(40, Protoss.Reaver, 100, 60)
    assert(!new ReaverShot(r).perform(retreating = false))
    assert(!new ReaverShot(r).perform(retreating = true))
    With.reaction = previousReaction
  }
  check("shot preparation reserves delivery, agency, a full turn and native attack startup") {
    assert(ReaverShot.preparationFrames(0, 1, 7) == 12)
    assert(ReaverShot.preparationFrames(4, 3, 7) == 18)
    assert(60 - ReaverShot.preparationFrames(0, 1, 7) == 48)
  }
  check("aiming uses a short accurate ray in every direction") {
    val origin = Pixel(500, 500)
    for (i <- 0 until 256) {
      val angle = i * Math.PI / 128
      val target = origin.add(Math.round(200 * Math.cos(angle)).toInt, Math.round(200 * Math.sin(angle)).toInt)
      val aim = ReaverShot.aimPoint(origin, target)
      assert(origin.pixelDistance(aim) <= ReaverShot.aimDistanceMax + 1)
      val error = Math.abs(Mathematics.Maff.radiansTo(origin.radiansToSlow(target), origin.radiansToSlow(aim)))
      assert(error <= Math.PI / 256, s"heading $i: $error")
    }
  }
  check("repeated aim updates stay anchored even when the target moves") {
    val origin = Pixel(500, 500)
    var position = origin
    var clipped = false
    for (i <- 0 to 100) {
      ReaverShot.aimPoint(position, Pixel(700, 450 + i), origin) match {
        case Some(to) => position = to
        case None => clipped = true
      }
      assert(position.pixelDistance(origin) <= ReaverShot.aimDistanceMax + 1)
    }
    assert(clipped)
  }
  println(s"$passed Reaver micro checks passed")
}
