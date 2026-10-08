package Placement

import Information.Geography.Geography
import Information.Geography.Types.{Base, Zone}
import Information.Grids.{Grids, GridUnits}
import Information.Grids.Construction._
import Information.Grids.Movement._
import Lifecycle.With
import Lifecycle.Configure.Configuration
import Mathematics.Points.{Point, Tile}
import Performance.Cache
import Placement.Access.PlaceLabels.TownHall
import Placement.Architecture.{Architecture => BuildingArchitecture, ArchitecturalAssessment}
import Placement.Templating.{RequireWalkable, Template, TemplatePointRequirement}
import Placement.Walls.{WallFinder, WallFillers}
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.ProxyBWAPI
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}

object CreepPlacementTest extends App {
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
  With.configuration = new Configuration
  With.configuration.debugging = false
  With.self = allocate(classOf[PlayerInfo])
  def race(value: bwapi.Race): Unit = set(With.self, "raceCurrentCache", new Cache(() => value))
  race(bwapi.Race.Protoss)
  With.proxy = allocate(classOf[ProxyBWAPI])
  set(With.proxy, "unitClasses", UnitTypes.all.map(UnitClass))
  With.grids = allocate(classOf[Grids])
  set(With.grids, "creep", new GridCreep)
  set(With.grids, "units", new GridUnits)
  set(With.grids, "buildable", new GridBuildable)
  set(With.grids, "walkable", new GridWalkable)
  set(With.grids, "unwalkableUnits", new GridUnwalkableUnits)
  set(With.grids, "buildableTerrain", new GridBuildableTerrain)
  set(With.grids, "walkableTerrain", new GridWalkableTerrain)
  set(With.grids, "buildableTownHall", new GridBuildableTownHall)
  set(With.grids, "psi2Height", new GridPsi2Height)
  set(With.grids, "psi3Height", new GridPsi3Height)
  for ((name, width, height) <- Seq(("buildable11", 1, 1), ("buildable22", 2, 2), ("buildable32", 3, 2), ("buildable43", 4, 3), ("buildable63", 6, 3))) {
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
  // No islands or resource paths in this fixture. Mark the synthetic lazy fields initialized.
  classOf[Zone].getDeclaredFields.filter(_.getName.startsWith("bitmap$")).foreach { field =>
    field.setAccessible(true)
    if (field.getType == java.lang.Long.TYPE) field.setLong(zone, -1L)
    else if (field.getType == java.lang.Integer.TYPE) field.setInt(zone, -1)
  }
  set(zone, "bases", Vector.empty[Base])
  val anchor = Tile(32, 32)
  val base = new Base("Creep fixture", anchor, Set.empty)
  set(With.geography, "_baseByTile", Array.fill[Option[Base]](With.mapTileArea)(Some(base)))
  set(With.geography, "_bases", Vector(base))
  With.architecture = new BuildingArchitecture
  With.grids.psi2Height.stamp(anchor)
  With.grids.psi3Height.stamp(anchor)

  var passed = 0
  def check(name: String)(body: => Unit): Unit = { body; passed += 1; println("PASS " + name) }
  def template(requirement: TemplatePointRequirement): Template = new Template().add(Point(0, 0), requirement)
  def withCreep(tile: Tile)(body: => Unit): Unit = {
    With.grids.creep.set(tile, true)
    try body finally { With.grids.creep.set(tile, false) }
  }
  check("non-Zerg templates reject creep anywhere in the footprint") {
    for (unitClass <- Seq(Protoss.Gateway, Protoss.Pylon, Terran.Barracks);
         dx <- 0 until unitClass.tileWidth; dy <- 0 until unitClass.tileHeight) {
      withCreep(anchor.add(dx, dy)) {
        assert(!new Placement().fitsAt(template(new TemplatePointRequirement(unitClass)), anchor))
      }
    }
  }
  check("generic non-Zerg slots and town hall reservations reject creep") {
    withCreep(anchor.add(1, 1)) {
      assert(!new Placement().fitsAt(template(new TemplatePointRequirement(4, 3)), anchor))
      assert(!new Placement().fitsAt(template(new TemplatePointRequirement(4, 3).withLabels(TownHall)), anchor))
    }
  }
  check("Zerg templates and walkways remain valid on creep") {
    withCreep(anchor.add(1, 1)) {
      assert(new Placement().fitsAt(template(new TemplatePointRequirement(Zerg.Hatchery)), anchor))
      assert(new Placement().fitsAt(template(new TemplatePointRequirement(Zerg.SpawningPool)), anchor))
      assert(new Placement().fitsAt(template(new RequireWalkable), anchor.add(1, 1)))
      race(bwapi.Race.Zerg)
      try {
        assert(new Placement().fitsAt(template(new TemplatePointRequirement(4, 3)), anchor))
        assert(new Placement().fitsAt(template(new TemplatePointRequirement(4, 3).withLabels(TownHall)), anchor))
      } finally { race(bwapi.Race.Protoss) }
    }
  }
  check("Zerg planning can still reserve space for future creep") {
    assert(new Placement().fitsAt(template(new TemplatePointRequirement(Zerg.SpawningPool)), anchor))
  }
  check("wall required buildings and filler cannot overlap remembered creep") {
    val wall = new WallFinder(zone, null, anchor, Seq.empty, WallFillers.PylonsCannons)
    for ((width, height) <- Seq((4, 3), (3, 2), (2, 2))) {
      assert(wall.canPlace(width, height, anchor, countMetrics = false))
      for (dx <- 0 until width; dy <- 0 until height) {
        withCreep(anchor.add(dx, dy)) { assert(!wall.canPlace(width, height, anchor, countMetrics = false)) }
      }
    }
  }
  check("runtime construction rejects cached creep for Protoss and Terran") {
    for (unitClass <- Seq(Protoss.Gateway, Protoss.Pylon, Terran.Barracks, Protoss.Nexus)) {
      assert(With.architecture.assess(anchor, unitClass) == ArchitecturalAssessment.Accepted)
      withCreep(anchor.add(1, 1)) {
        assert(With.architecture.assess(anchor, unitClass) == ArchitecturalAssessment.CreepMismatch)
      }
    }
  }
  check("Hatcheries accept either terrain; creep-requiring Zerg buildings require the whole footprint") {
    assert(With.architecture.assess(anchor, Zerg.Hatchery) == ArchitecturalAssessment.Accepted)
    withCreep(anchor.add(1, 1)) {
      assert(With.architecture.assess(anchor, Zerg.Hatchery) == ArchitecturalAssessment.Accepted)
      assert(With.architecture.assess(anchor, Zerg.SpawningPool) == ArchitecturalAssessment.CreepMismatch)
    }
    for (x <- 0 until Zerg.SpawningPool.tileWidth; y <- 0 until Zerg.SpawningPool.tileHeight) With.grids.creep.set(anchor.add(x, y), true)
    try { assert(With.architecture.assess(anchor, Zerg.SpawningPool) == ArchitecturalAssessment.Accepted) }
    finally { With.grids.creep.reset() }
  }
  println(s"Creep placement: $passed passed")
}
