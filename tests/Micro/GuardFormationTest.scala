package Micro.Formation

import Information.Geography.Geography
import Information.Geography.Types.Zone
import Information.Grids.Grids
import Information.Grids.Floody.GridEnemyRangeGround
import Information.Grids.Miscellaneous.GridFormationSlots
import Information.Grids.Movement.{GridUnwalkableUnits, GridWalkable, GridWalkableTerrain}
import Lifecycle.With
import Mathematics.Points.Pixel
import Performance.Cache
import ProxyBwapi.ProxyBWAPI
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import ProxyBwapi.UnitTracking.UnitTracker

object GuardFormationTest extends App {
  val unsafeField = classOf[sun.misc.Unsafe].getDeclaredField("theUnsafe")
  unsafeField.setAccessible(true)
  val unsafe = unsafeField.get(null).asInstanceOf[sun.misc.Unsafe]
  def allocate[T](clazz: Class[T]): T = unsafe.allocateInstance(clazz).asInstanceOf[T]
  def set(obj: AnyRef, name: String, value: Any): Unit = {
    val fields = Iterator.iterate[Class[_]](obj.getClass)(_.getSuperclass).takeWhile(_ != null)
      .flatMap(_.getDeclaredFields).filter(f => f.getName == name || f.getName.endsWith("$$" + name)).toVector
    assert(fields.nonEmpty, name)
    fields.foreach(f => { f.setAccessible(true); f.set(obj, value.asInstanceOf[AnyRef]) })
  }
  var passed = 0
  def check(name: String)(body: => Unit): Unit = { body; passed += 1; println("PASS " + name) }

  With.frame = 100
  With.mapTileWidth = 128; With.mapTileHeight = 128; With.mapTileArea = 128 * 128
  With.mapPixelWidth = 4096; With.mapPixelHeight = 4096
  With.proxy = allocate(classOf[ProxyBWAPI])
  set(With.proxy, "unitClasses", UnitTypes.all.map(UnitClass))
  With.units = new UnitTracker
  With.game = allocate(classOf[bwapi.Game])
  set(With.game, "mapWidth", 128); set(With.game, "mapHeight", 128)
  set(With.game, "groundHeight", Array.fill(128, 128)(0))
  With.geography = allocate(classOf[Geography])
  set(With.geography, "_zoneByTile", Array.fill(With.mapTileArea)(allocate(classOf[Zone])))
  With.grids = allocate(classOf[Grids])
  set(With.grids, "walkable", new GridWalkable)
  val terrain = new GridWalkableTerrain
  terrain.indices.foreach(terrain.set(_, true))
  set(With.grids, "walkableTerrain", terrain)
  set(With.grids, "unwalkableUnits", new GridUnwalkableUnits)
  set(With.grids, "enemyRangeGround", new GridEnemyRangeGround)
  set(With.grids, "formationSlots", new GridFormationSlots)

  def building(pixel: Pixel, kind: UnitClass = Protoss.PhotonCannon): FriendlyUnitInfo = {
    val u = allocate(classOf[FriendlyUnitInfo])
    set(u, "_unitClass", kind)
    set(u, "_pixel", pixel)
    set(u, "pixelRangeAirCache", new Cache(() => kind.pixelRangeAir))
    set(u, "pixelRangeGroundCache", new Cache(() => kind.pixelRangeGround))
    u
  }
  val choke = Pixel(1000, 1000)
  val inward = Pixel(1000, 1032)
  val radius = 96d
  def parameterized(geometry: GuardGeometry, goal: Pixel): FormationStandard = {
    val f = allocate(classOf[FormationStandard])
    set(f, "style", FormationStyleGuard)
    set(f, "guardGeometry", Some(geometry))
    set(f, "goal", goal)
    set(f, "expectRangeTiles", 1)
    set(f, "apex", goal); set(f, "face", goal)
    set(f, "minAltitude", -1); set(f, "maxThreat", 0)
    val method = classOf[FormationStandard].getDeclaredMethods.find(_.getName.endsWith("parameterize")).get
    method.setAccessible(true)
    method.invoke(f)
    f
  }
  // Exercise the production slot generator without unrelated battle/pathfinding setup.
  def slots(f: FormationStandard, count: Int = 16): Vector[Pixel] = {
    With.grids.formationSlots.reset()
    val slotClass = Class.forName("Micro.Formation.FormationStandard$ClassSlots")
    val constructor = slotClass.getDeclaredConstructors.head
    constructor.setAccessible(true)
    val request = constructor.newInstance(f, Protoss.Zealot, Int.box(count), Double.box(0.0))
    val method = classOf[FormationStandard].getDeclaredMethods.find(_.getName.endsWith("arcSlots")).get
    method.setAccessible(true)
    method.invoke(f, f.face, Vector(request), Double.box(32 + f.apex.pixelDistance(f.face)))
      .asInstanceOf[Map[UnitClass, Seq[Pixel]]].values.flatten.toVector
  }

  check("the wall goal cannot reverse the arc in any cardinal direction") {
    Vector(Pixel(1032, 1000), Pixel(1000, 1032), Pixel(968, 1000), Pixel(1000, 968)).foreach(inside => {
      val g = new GuardGeometry(choke, inside, radius, Vector.empty)
      val wallGoal = choke.project(inside, 200)
      val f = parameterized(g, wallGoal)
      assert(g.center == choke)
      assert(g.facesOutward(f.face))
      assert(!g.facesOutward(f.apex))
      assert(slots(f, 4).size == 4)
    })
  }
  check("already-covered choke centers do not move") {
    val cannon = building(Pixel(1000, 1100))
    val g = new GuardGeometry(choke, inward, radius, Vector(cannon))
    assert(g.defenses == Vector(cannon) && g.center == choke)
  }
  check("projection uses building-edge distance and stops at the first covered pixel") {
    val cannon = building(Pixel(1000, 1300))
    val g = new GuardGeometry(choke, inward, radius, Vector(cannon))
    assert(g.center == Pixel(1000, cannon.top - cannon.pixelRangeMax.toInt))
    assert(g.covered(g.center) && !g.covered(g.center.add(0, -1)))
    assert(g.center.y - choke.y <= radius)
  }
  check("every nearby static defense must cover the projected center") {
    val first = building(Pixel(1000, 1260))
    val second = building(Pixel(1000, 1320))
    val g = new GuardGeometry(choke, inward, radius, Vector(first, second))
    assert(g.defenses.size == 2)
    assert(g.center.y == second.top - second.pixelRangeMax.toInt)
    assert(g.defenses.forall(b => b.pixelDistanceEdge(g.center) <= b.pixelRangeMax))
  }
  check("non-defense buildings and defenses at or beyond range plus radius are excluded") {
    val cannon = building(Pixel(1000, 1100))
    val boundary = building(Pixel(1000, 1000 + cannon.pixelRangeMax.toInt + radius.toInt + cannon.unitClass.dimensionUp))
    val far = building(Pixel(2000, 2000))
    val pylon = building(choke, Protoss.Pylon)
    val g = new GuardGeometry(choke, inward, radius, Vector(cannon, boundary, far, pylon))
    assert(g.defenses == Vector(cannon))
  }
  check("projection cannot exceed a fractional choke radius when coverage is impossible") {
    val cannon = building(Pixel(1000, 720))
    val g = new GuardGeometry(choke, inward, 95.5, Vector(cannon))
    assert(g.defenses.nonEmpty && !g.covered(g.center))
    assert(g.center == Pixel(1000, 1095))
    assert(slots(parameterized(g, choke)).isEmpty)
  }
  check("arc ends and later rows must stay within every supporting building's range") {
    val cannons = Vector(building(Pixel(980, 1250)), building(Pixel(1100, 1240)))
    val g = new GuardGeometry(choke, inward, radius, cannons)
    val f = parameterized(g, choke)
    val covered = slots(f, 40)
    assert(covered.nonEmpty)
    assert(covered.forall(p => cannons.forall(b => b.pixelDistanceEdge(p) <= b.pixelRangeMax)))
    set(f, "guardGeometry", None)
    assert(slots(f, 40).exists(p => !g.covered(p)), "Fixture must expose unsupported arc slots without the coverage check")
  }
  check("an inward alternate face cannot undo the corrected facing") {
    val g = new GuardGeometry(choke, inward, radius, Vector.empty)
    assert(g.facesOutward(choke.add(0, -96)))
    assert(!g.facesOutward(choke.add(0, 96)))
    assert(!g.facesOutward(choke.add(96, 0)))
  }
  check("the Colosseum replay's wall gap reverses the old facing, while the new arc has cannon cover") {
    // GAME_PB_F203670D99_0054 at 4:56: BWTA banks and completed cannons from the replay/map logs.
    val center = Pixel(804, 3128)
    val inside = Pixel(778, 3145)
    val width = 133.1465358167459
    val cannons = Vector(building(Pixel(672, 3360)), building(Pixel(608, 3360)))
    val wallGap = Pixel(656, 3248)
    val g = new GuardGeometry(center, inside, width / 2, cannons)
    val oldApex = center.project(inside, 32)
    val oldFace = oldApex.project(wallGap, 32 + width / 2)
    assert(!g.facesOutward(oldFace))
    val f = parameterized(g, wallGap)
    assert(g.facesOutward(f.face))
    assert(g.defenses.size == 2 && g.covered(g.center))
    assert(Math.sqrt(g.center.pixelDistanceSquared(center)) <= width / 2)
    val positions = slots(f, 5)
    assert(positions.size == 5)
    assert(positions.forall(g.covered))
    println(s"Colosseum choke=$center supportedCenter=${g.center} apex=${f.apex} face=${f.face} slots=${positions.mkString(",")}")
  }
  println(s"$passed guard formation tests passed")
}
