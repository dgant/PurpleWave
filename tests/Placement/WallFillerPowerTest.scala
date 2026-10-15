package Placement

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Lifecycle.Configure.Configuration
import Mathematics.Points.{Point, Tile}
import Mathematics.Shapes.Pylons
import Placement.Walls.{Wall, WallConstraint, WallFinder, WallFillers, WallProblems, WallSpans}
import ProxyBwapi.ProxyBWAPI
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}

import scala.collection.mutable.ArrayBuffer

object WallFillerPowerTest extends App {
  val unsafeField = classOf[sun.misc.Unsafe].getDeclaredField("theUnsafe")
  unsafeField.setAccessible(true)
  val unsafe = unsafeField.get(null).asInstanceOf[sun.misc.Unsafe]
  def allocate[T](clazz: Class[T]): T = unsafe.allocateInstance(clazz).asInstanceOf[T]
  def set(obj: AnyRef, name: String, value: Any): Unit = {
    val field = obj.getClass.getDeclaredField(name)
    field.setAccessible(true)
    field.set(obj, value.asInstanceOf[AnyRef])
  }

  With.mapTileWidth = 128
  With.mapTileHeight = 128
  With.mapTileArea = 128 * 128
  With.configuration = new Configuration
  With.configuration.debugging = false
  With.proxy = allocate(classOf[ProxyBWAPI])
  set(With.proxy, "unitClasses", UnitTypes.all.map(UnitClass))
  val zone = allocate(classOf[Zone])
  classOf[Zone].getDeclaredFields.filter(_.getName.startsWith("bitmap$")).foreach { field =>
    field.setAccessible(true)
    if (field.getType == java.lang.Long.TYPE) field.setLong(zone, -1L)
    else if (field.getType == java.lang.Integer.TYPE) field.setInt(zone, -1)
  }
  set(zone, "bases", Vector.empty[Base])
  set(zone, "tiles", Set.empty[Tile])

  var passed = 0
  def check(name: String)(body: => Unit): Unit = { body; passed += 1; println("PASS " + name) }
  def fill(required: Seq[(Tile, UnitClass)], slots: Seq[Tile]): (WallFinder, Wall) = {
    // Supply terrain-valid slots to exercise the real pylon/cannon configuration search.
    val finder = new WallFinder(zone, null, Tile(0, 0), Seq.empty, WallFillers.PylonsCannons) {
      override def fill2x2Offset(offset: Point, placements: ArrayBuffer[Tile], inside: Set[Tile]): Unit = placements ++= slots
    }
    val wall = new Wall
    wall.constraint = WallConstraint(1, Protoss.Dragoon, WallSpans.TerrainTerrain, required.map(_._2): _*)
    wall.buildings ++= required
    finder.wall = Some(wall)
    finder.fill2X2()
    (finder, wall)
  }

  val replayRequired = Seq((Tile(30, 112), Protoss.Gateway), (Tile(35, 113), Protoss.Forge))
  val replayCannons = Seq(Tile(29, 118), Tile(29, 120), Tile(27, 118), Tile(27, 120), Tile(31, 122), Tile(29, 122))
  val replayPylons = Seq(Tile(35, 116), Tile(33, 122))
  check("replay fixture has six eventual cannons but only one powered by the initial pylon") {
    assert(replayRequired.forall(b => Pylons.powers(replayPylons.head, b._1, b._2)))
    assert(replayCannons.count(Pylons.powers(replayPylons.head, _, Protoss.PhotonCannon)) == 1)
    assert(replayCannons.forall(Pylons.powers(replayPylons.last, _, Protoss.PhotonCannon)))
  }
  check("reject the Vermeer wall when its initial filler pylon powers only one cannon") {
    val (finder, wall) = fill(replayRequired, replayPylons ++ replayCannons)
    assert(finder.wall.isEmpty, "Accepted the replay's one-cannon initial pylon")
    assert(wall.problems.contains(WallProblems.InsufficientFiller))
  }
  check("reject the same one-cannon layout when the first pylon is required by the wall") {
    val (finder, _) = fill(replayRequired :+ (replayPylons.head, Protoss.Pylon), replayPylons.tail ++ replayCannons)
    assert(finder.wall.isEmpty)
  }
  check("a later pylon cannot compensate for an initial pylon that leaves the Forge unpowered") {
    val (finder, _) = fill(replayRequired :+ (replayPylons.last, Protoss.Pylon), replayPylons.take(1) ++ replayCannons)
    assert(finder.wall.isEmpty)
  }
  for (initialCount <- Seq(2, 3)) check(s"generation rejects an initial pylon powering only $initialCount cannons") {
    val twoPowered = replayCannons.updated(2, Tile(29, 116))
    val cannons = if (initialCount == 2) twoPowered else twoPowered.updated(1, Tile(31, 118))
    assert(cannons.count(Pylons.powers(replayPylons.head, _, Protoss.PhotonCannon)) == initialCount)
    assert(cannons.forall(c => replayPylons.exists(Pylons.powers(_, c, Protoss.PhotonCannon))))
    val (finder, wall) = fill(replayRequired :+ (replayPylons.head, Protoss.Pylon), replayPylons.tail ++ cannons)
    assert(finder.wall.isEmpty, s"Accepted a wall with only $initialCount initially powered cannons")
    assert(wall.problems.contains(WallProblems.InsufficientFiller))
  }
  check("accept a filler pylon powering exactly four cannons and both required buildings") {
    val cannons = Seq(Tile(29, 118), Tile(31, 118), Tile(33, 118), Tile(37, 118), Tile(31, 122), Tile(29, 122))
    val (finder, wall) = fill(replayRequired, replayPylons ++ cannons)
    assert(finder.wall.contains(wall))
    val firstPylon = wall.buildings.find(_._2 == Protoss.Pylon).get._1
    val placedCannons = wall.buildings.filter(_._2 == Protoss.PhotonCannon).map(_._1)
    assert(firstPylon == replayPylons.head)
    assert(placedCannons.size == 6)
    assert(placedCannons.count(Pylons.powers(firstPylon, _, Protoss.PhotonCannon)) == 4)
  }
  check("accept a required pylon powering exactly four cannons with later expansion") {
    val required = Seq((Tile(96, 12), Protoss.Forge), (Tile(91, 13), Protoss.Gateway), (Tile(89, 13), Protoss.Pylon))
    val slots = Seq(Tile(93, 11), Tile(91, 11), Tile(89, 11), Tile(97, 9), Tile(87, 11), Tile(97, 7), Tile(97, 5), Tile(99, 7))
    val (finder, wall) = fill(required, slots)
    assert(finder.wall.contains(wall))
    val pylons = wall.buildings.filter(_._2 == Protoss.Pylon).map(_._1)
    val cannons = wall.buildings.filter(_._2 == Protoss.PhotonCannon).map(_._1)
    assert(pylons.head == Tile(89, 13))
    assert(required.filter(_._2.requiresPsi).forall(b => Pylons.powers(pylons.head, b._1, b._2)))
    assert(cannons.count(Pylons.powers(pylons.head, _, Protoss.PhotonCannon)) == 4)
    assert(cannons.size >= 6)
    assert(cannons.forall(c => pylons.exists(Pylons.powers(_, c, Protoss.PhotonCannon))))
    assert(cannons.exists(c => !Pylons.powers(pylons.head, c, Protoss.PhotonCannon)))
  }
  println(s"Wall filler power: $passed passed")
}
