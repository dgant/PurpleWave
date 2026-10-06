package Placement

import Information.Geography.Geography
import Information.Geography.Types.{Base, Zone}
import Information.Grids.Grids
import Information.Grids.Construction._
import Information.Grids.Movement._
import Lifecycle.With
import Mathematics.Points.{Point, Tile}
import Placement.Access.PlaceLabels._
import Placement.Generation.{Fit, TemplatesGeneric, TemplatesProtoss, TemplatesTerran, TemplatesZerg}
import Placement.Templating._
import ProxyBwapi.ProxyBWAPI
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}

object PlacementReservationTest extends App {
  // Only geography and terrain are synthetic. Exercise the production fitter,
  // template parser, indexes and cache without starting a BWAPI game.
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

  With.mapTileWidth = 64
  With.mapTileHeight = 64
  With.mapTileArea = 64 * 64
  With.proxy = allocate(classOf[ProxyBWAPI])
  set(With.proxy, "unitClasses", UnitTypes.all.map(UnitClass))
  With.grids = allocate(classOf[Grids])
  set(With.grids, "buildable", new GridBuildable)
  set(With.grids, "walkable", new GridWalkable)
  set(With.grids, "unwalkableUnits", new GridUnwalkableUnits)
  set(With.grids, "buildableTerrain", new GridBuildableTerrain)
  set(With.grids, "walkableTerrain", new GridWalkableTerrain)
  set(With.grids, "buildableTownHall", new GridBuildableTownHall)
  Seq(("buildable11", 1, 1), ("buildable22", 2, 2), ("buildable32", 3, 2), ("buildable43", 4, 3)).foreach {
    case (name, width, height) =>
      val grid = new GridBuildableWH(width, height)
      java.util.Arrays.fill(grid.rawValues, true)
      set(With.grids, name, grid)
  }
  java.util.Arrays.fill(With.grids.buildableTerrain.rawValues, true)
  java.util.Arrays.fill(With.grids.walkableTerrain.rawValues, true)
  java.util.Arrays.fill(With.grids.buildableTownHall.rawValues, true)
  With.geography = allocate(classOf[Geography])
  val zone = allocate(classOf[Zone])
  set(With.geography, "_zoneByTile", Array.fill(With.mapTileArea)(zone))
  val hallTile = Tile(32, 32)
  val base = new Base("Fixture", hallTile, Set.empty)
  set(With.geography, "_baseByTile", Array.fill[Option[Base]](With.mapTileArea)(Some(base)))
  set(With.geography, "_bases", Vector(base))

  var passed = 0
  var failed = 0
  def check(name: String)(test: => Unit): Unit = {
    try { test; passed += 1; println("PASS " + name) }
    catch { case error: AssertionError => failed += 1; println("FAIL " + name + ": " + error.getMessage) }
  }
  def template(requirement: TemplatePointRequirement, offset: Point = Point(0, 0)): Template =
    new Template().add(offset, requirement)
  def hall: TemplatePointRequirement = new TemplatePointRequirement(4, 3).withLabels(TownHall)
  def pylon: TemplatePointRequirement = new TemplatePointRequirement(Protoss.Pylon)
  def canReuse(previous: TemplatePointRequirement, next: TemplatePointRequirement, shift: Point = Point(0, 0)): Boolean = {
    val placement = new Placement
    placement.index(Fit(hallTile, template(previous)))
    placement.fitsAt(template(next), hallTile.add(shift))
  }

  check("town hall reuses its existing reservation") { assert(canReuse(hall, hall)) }
  check("proxy Gateway blocks town hall") { assert(!canReuse(new TemplatePointRequirement(Protoss.Gateway), hall)) }
  check("generic proxy slot blocks town hall") { assert(!canReuse(new TemplatePointRequirement(4, 3), hall)) }
  check("town hall blocks generic proxy slot") { assert(!canReuse(hall, new TemplatePointRequirement(4, 3))) }
  check("town hall still requires the base's exact anchor") {
    assert(!new Placement().fitsAt(template(hall), hallTile.add(1, 0)))
  }
  check("matching Pylon reservation is reusable") { assert(canReuse(pylon, pylon)) }
  check("shifted Pylon cannot overlap") { assert(!canReuse(pylon, pylon, Point(1, 0))) }
  check("partially overlapping Pylons cannot reuse an interior cell as their anchor") {
    for (dx <- -1 to 1; dy <- -1 to 1 if dx != 0 || dy != 0) assert(!canReuse(pylon, pylon, Point(dx, dy)))
  }
  check("matching footprint cannot run outside map bounds") {
    val placement = new Placement
    val edge = Tile(With.mapTileWidth - 1, With.mapTileHeight - 1)
    placement.index(Fit(edge, template(pylon)))
    assert(!placement.fitsAt(template(pylon), edge))
  }
  check("different dimensions cannot overlap") { assert(!canReuse(new TemplatePointRequirement(2, 2), new TemplatePointRequirement(3, 2))) }
  check("different allowed building types cannot overlap") { assert(!canReuse(pylon, new TemplatePointRequirement(Protoss.PhotonCannon))) }
  check("allowed types are a set, not an ordering") {
    assert(canReuse(new TemplatePointRequirement(Protoss.Pylon, Protoss.PhotonCannon), new TemplatePointRequirement(Protoss.PhotonCannon, Protoss.Pylon)))
  }
  check("allowed types cannot be broadened or narrowed") {
    val multiple = new TemplatePointRequirement(Protoss.Pylon, Protoss.PhotonCannon)
    assert(!canReuse(pylon, multiple))
    assert(!canReuse(multiple, pylon))
  }
  check("macro hatch cannot reuse a town hall reservation") {
    assert(!canReuse(hall, new TemplatePointRequirement(4, 3).withLabels(MacroHatch)))
  }
  check("each occupancy flag is part of reservation compatibility") {
    val variants = Seq(
      new TemplatePointRequirement(2, 2) { override val buildableBefore = false },
      new TemplatePointRequirement(2, 2) { override val walkableBefore = false },
      new TemplatePointRequirement(2, 2) { override val buildableAfter = true },
      new TemplatePointRequirement(2, 2) { override val walkableAfter = true })
    variants.foreach(next => assert(!canReuse(new TemplatePointRequirement(2, 2), next)))
  }
  check("walkways can overlap walkways") { assert(canReuse(new RequireWalkable, new RequireWalkable)) }
  check("buildings cannot overwrite walkways") { assert(!canReuse(new RequireWalkable, pylon)) }
  check("walkways cannot cross buildings") { assert(!canReuse(pylon, new RequireWalkable)) }
  check("matching reservation does not bypass physical buildability") {
    With.grids.buildableTerrain.set(hallTile.add(1, 1), false)
    try { assert(!canReuse(hall, hall)) }
    finally { With.grids.buildableTerrain.set(hallTile.add(1, 1), true) }
  }
  check("matching reservation does not bypass physical walkability") {
    With.grids.walkableTerrain.set(hallTile.add(1, 1), false)
    try { assert(!canReuse(hall, hall)) }
    finally { With.grids.walkableTerrain.set(hallTile.add(1, 1), true) }
  }
  check("matching reservation does not bypass real unit occupancy") {
    With.grids.unwalkableUnits.stamp(hallTile.add(1, 1))
    try { assert(!canReuse(hall, hall)) }
    finally { With.grids.unwalkableUnits.reset() }
  }
  check("footprint grid stores local offsets, foundation keeps template offset") {
    val placement = new Placement
    val offset = Point(5, 7)
    val origin = hallTile.subtract(offset.x, offset.y)
    placement.index(Fit(origin, template(hall, offset)))
    for (dx <- 0 until 4; dy <- 0 until 3) assert(placement.at(hallTile.add(dx, dy)).point == Point(dx, dy))
    assert(placement.get(hallTile).get.point.point == offset)
    assert(placement.fitsAt(template(hall), hallTile))
  }
  check("reused labels survive in tile and label indexes without mutating templates") {
    val placement = new Placement
    val first = pylon.withLabels(DefendHall)
    val second = pylon.withLabels(DefendAir)
    placement.index(Fit(hallTile, template(first)))
    placement.index(Fit(hallTile, template(second)))
    val merged = placement.get(hallTile).get.point.requirement
    assert(merged.labels.toSet == Set(DefendHall, DefendAir))
    assert(placement.at(hallTile.add(1, 1)).requirement.labels.toSet == merged.labels.toSet)
    assert(placement.get(DefendHall).exists(_ == placement.get(hallTile).get))
    assert(placement.get(DefendAir).exists(_ == placement.get(hallTile).get))
    assert(first.labels.toSet == Set(DefendHall))
    assert(second.labels.toSet == Set(DefendAir))
  }
  check("real constrained defense template fits around a reserved hall") {
    val placement = new Placement
    placement.index(Fit(hallTile, TemplatesGeneric.townhall))
    val fits = placement.fitAndIndexConstrained(5, 1, Seq(TemplatesProtoss.expansions.last), base)
    assert(fits.nonEmpty)
    assert(placement.get(DefendHall).exists(_.point.requirement.buildings.contains(Protoss.Pylon)))
    assert(placement.get(DefendHall).exists(_.point.requirement.buildings.contains(Protoss.PhotonCannon)))
  }
  check("center proxy blocks the whole constrained defense layout") {
    val placement = new Placement
    placement.index(Fit(hallTile, template(new TemplatePointRequirement(4, 3).withLabels(Proxy2Gate))))
    assert(!placement.fitsAt(TemplatesGeneric.townhall, hallTile))
    assert(placement.fitAndIndexConstrained(5, 1, Seq(TemplatesProtoss.expansions.last), base).isEmpty)
    assert(placement.get(Proxy2Gate).nonEmpty)
    assert(placement.get(DefendHall).isEmpty)
  }
  check("Terran and Zerg base layouts also reuse reserved halls") {
    for (layout <- Seq(TemplatesTerran.bases(4), TemplatesZerg.mainBases(2))) {
      val placement = new Placement
      placement.index(Fit(hallTile, TemplatesGeneric.townhall))
      assert(placement.fitAndIndexConstrained(5, 1, Seq(layout), base).nonEmpty)
      assert(placement.get(TownHall).nonEmpty)
      assert(placement.foundations.exists(!_.point.requirement.isTownHall))
    }
  }
  check("placement cache replay preserves local offsets, label unions and fit order") {
    val placement = new Placement
    placement.index(Fit(hallTile, TemplatesGeneric.townhall))
    assert(placement.fitAndIndexConstrained(5, 1, Seq(TemplatesProtoss.expansions.last), base).nonEmpty)
    val labelledTile = Tile(10, 10)
    placement.index(Fit(labelledTile, template(pylon.withLabels(DefendHall)), 3))
    placement.index(Fit(labelledTile, template(pylon.withLabels(DefendAir)), 1))
    val keyConstructor = Class.forName("Placement.PlacementCache$CacheKey").getDeclaredConstructors.head
    keyConstructor.setAccessible(true)
    val key = keyConstructor.newInstance("fixture", Int.box(2), Int.box(0), Int.box(hallTile.x), Int.box(hallTile.y)).asInstanceOf[AnyRef]
    def cacheCall(name: String, arguments: AnyRef*): AnyRef = {
      val method = PlacementCache.getClass.getDeclaredMethods.find(m => m.getName == name && m.getParameterCount == arguments.size).get
      method.setAccessible(true)
      method.invoke(PlacementCache, arguments: _*)
    }
    val file = java.io.File.createTempFile("placement-reservations-", ".bin.gz")
    try {
      cacheCall("write", file, "reservation-test", key, placement)
      val result = cacheCall("read", file, "reservation-test", key).asInstanceOf[Product]
      assert(result.productPrefix == "Hit", result.toString)
      val restored = new Placement
      cacheCall("restore", restored, result.productElement(0).asInstanceOf[AnyRef])
      restored.finishCachedRestore()
      def signature(p: Placement): Seq[Any] = (0 until With.mapTileArea).map { i =>
        val point = p.at(new Tile(i))
        val r = point.requirement
        (point.point, r.dimensions, r.buildableBefore, r.walkableBefore, r.buildableAfter, r.walkableAfter, r.buildings.toSet, r.labels.toSet)
      }
      assert(signature(restored) == signature(placement))
      assert(restored.get(labelledTile).get.point.requirement.labels.toSet == Set(DefendHall, DefendAir))
      assert(restored.get(DefendHall).exists(_.tile == labelledTile))
      assert(restored.fitsIndexOrder.map(_.order) == placement.fitsIndexOrder.map(_.order))
      assert(restored.fitsAt(template(hall), hallTile))
      val stale = cacheCall("read", file, "different-revision", key).asInstanceOf[Product]
      assert(stale.productPrefix == "Stale")
    } finally { assert(file.delete(), "Could not remove temporary test cache") }
  }

  println(s"Placement reservations: $passed passed, $failed failed")
  assert(failed == 0, "Placement reservation regressions failed")
}
