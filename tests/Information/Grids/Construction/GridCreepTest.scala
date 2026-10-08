package Information.Grids.Construction

import Information.Grids.Grids
import Lifecycle.With
import Mathematics.Points.Tile
import bwapi.{Race, UnitType}

object GridCreepTest extends App {
  import GridCreep._

  With.mapTileWidth = 64
  With.mapTileHeight = 48
  With.mapTileArea = With.mapTileWidth * With.mapTileHeight

  class Fixture(buildings: Seq[InitialBuilding] = Seq.empty, races: Seq[Race] = Seq(Race.Protoss, Race.Terran)) extends GridCreep {
    val visible = new Array[Boolean](length)
    val actual = new Array[Boolean](length)
    val buildable = Array.fill(length)(true)
    val walkable = Array.fill(length * 16)(true)
    var visibilityReads, creepReads, terrainReads = 0
    override protected def initialBuildings: Seq[InitialBuilding] = buildings
    override protected def initialPlayerRaces: Seq[Race] = races
    override protected def isVisible(x: Int, y: Int): Boolean = { visibilityReads += 1; visible(x + y * width) }
    override protected def liveCreep(x: Int, y: Int): Boolean = { creepReads += 1; actual(x + y * width) }
    override protected def isBuildable(x: Int, y: Int): Boolean = { terrainReads += 1; buildable(x + y * width) }
    override protected def isWalkable(x: Int, y: Int): Boolean = { terrainReads += 1; walkable(x + y * width * 4) }
  }

  def building(t: UnitType, x: Int = 30, y: Int = 22, neutral: Boolean = true): InitialBuilding =
    InitialBuilding(t, x * 32 + t.tileWidth * 16, y * 32 + t.tileHeight * 16, neutral)
  def start(grid: Fixture): Fixture = { With.frame = 0; grid.update(); grid }
  var passed = 0
  def check(name: String)(body: => Unit): Unit = { body; passed += 1; println("PASS " + name) }

  check("non-Zerg games never scan visibility or creep, including subsequent updates") {
    val grid = start(new Fixture(Seq(building(UnitType.Protoss_Pylon))))
    for (frame <- Seq(1, 12, 120, 10000)) { With.frame = frame; grid.update() }
    assert(!grid.hadNeutralZergBuildings && !grid.hadZergPlayers)
    assert(grid.visibilityReads == 0 && grid.creepReads == 0 && grid.terrainReads == 0)
    assert(!grid.rawValues.contains(true))
  }
  check("neutral Zerg buildings enable observations without a Zerg player") {
    val grid = start(new Fixture(Seq(building(UnitType.Zerg_Spawning_Pool))))
    assert(grid.hadNeutralZergBuildings && !grid.hadZergPlayers)
    assert(grid.visibilityReads == grid.length)
    assert(grid.getUnchecked(30, 22) && grid.initiallyNeutral(30 + 22 * grid.width))
    assert(!grid.getUnchecked(29, 22), "non-spreading structures only seed their footprint")
    assert(grid.rawValues.count(identity) == UnitType.Zerg_Spawning_Pool.tileWidth * UnitType.Zerg_Spawning_Pool.tileHeight)
  }
  check("friendly starting Hatchery seeds fogged creep and records the Zerg player") {
    val grid = start(new Fixture(Seq(building(UnitType.Zerg_Hatchery, neutral = false)), Seq(Race.Zerg, Race.Terran)))
    assert(grid.hadZergPlayers && !grid.hadNeutralZergBuildings)
    assert(grid.getUnchecked(25, 22) && grid.getUnchecked(38, 22))
    assert(!grid.initiallyNeutral(25 + 22 * grid.width))
    assert(!grid.getUnchecked(42, 22) && !grid.getUnchecked(32, 30))
  }
  check("Random and unknown opponents keep tracking enabled without claiming a known Zerg player") {
    for (race <- Seq(Race.Random, Race.Unknown)) {
      val grid = new Fixture(races = Seq(Race.Protoss, race))
      grid.visible(50) = true
      grid.actual(50) = true
      start(grid)
      assert(!grid.hadZergPlayers && grid.getUnchecked(50))
      assert(grid.creepReads == 1)
    }
  }
  check("neutral non-spreading Zerg buildings still enable the cache") {
    val grid = start(new Fixture(Seq(building(UnitType.Zerg_Extractor))))
    assert(grid.hadNeutralZergBuildings && grid.visibilityReads > 0)
  }
  check("unbuildable, partially walkable and above-unbuildable terrain excludes creep") {
    val grid = new Fixture(Seq(building(UnitType.Zerg_Hatchery)))
    grid.buildable(34 + 25 * grid.width) = false
    grid.walkable(28 * 4 + 22 * 4 * grid.width * 4) = false
    start(grid)
    assert(!grid.getUnchecked(34, 25) && !grid.getUnchecked(34, 24))
    assert(!grid.getUnchecked(28, 22))
    assert(grid.getUnchecked(27, 22))
  }
  check("other buildings block spread but a source seeds its own occupied footprint") {
    val grid = start(new Fixture(Seq(building(UnitType.Zerg_Hatchery), building(UnitType.Protoss_Pylon, 35, 22))))
    assert(grid.getUnchecked(30, 22))
    for (x <- 35 to 36; y <- 22 to 23) assert(!grid.getUnchecked(x, y))
  }
  check("creep does not jump across disconnected terrain inside its range") {
    val grid = new Fixture(Seq(building(UnitType.Zerg_Hatchery)))
    for (y <- 0 until grid.height) grid.buildable(35 + y * grid.width) = false
    start(grid)
    assert(grid.getUnchecked(34, 22))
    assert(!grid.getUnchecked(36, 22))
  }
  check("seed and range clip correctly at all four map edges") {
    for (x <- Seq(0, With.mapTileWidth - 4); y <- Seq(0, With.mapTileHeight - 3)) {
      val grid = start(new Fixture(Seq(building(UnitType.Zerg_Hatchery, x, y))))
      assert(grid.getUnchecked(x, y))
      assert(grid.rawValues.count(identity) > 12)
    }
  }
  check("last-seen creep survives fog, and visible absence clears it") {
    val grid = new Fixture(races = Seq(Race.Zerg, Race.Protoss))
    grid.visible(50) = true
    grid.actual(50) = true
    start(grid)
    assert(grid.getUnchecked(50))
    grid.visible(50) = false
    grid.actual(50) = false
    With.frame = 12
    grid.update()
    assert(grid.getUnchecked(50) && grid.creepReads == 1)
    grid.visible(50) = true
    With.frame = 24
    grid.update()
    assert(!grid.getUnchecked(50) && grid.creepReads == 2)
  }
  check("visible observations override frame-zero predictions without reseeding") {
    val grid = new Fixture(Seq(building(UnitType.Zerg_Hatchery)))
    val i = 30 + 22 * grid.width
    grid.visible(i) = true
    start(grid)
    assert(!grid.getUnchecked(i) && grid.initiallyNeutral(i))
    grid.visible(i) = false
    With.frame = 24
    grid.update()
    assert(!grid.getUnchecked(i))
  }
  check("observations are throttled and only visible tiles read BWAPI creep") {
    val grid = new Fixture(races = Seq(Race.Zerg))
    grid.visible(50) = true
    start(grid)
    val reads = grid.visibilityReads
    grid.actual(50) = true
    for (frame <- 0 until ObservationPeriod) { With.frame = frame; grid.update() }
    assert(grid.visibilityReads == reads && grid.creepReads == 1 && !grid.getUnchecked(50))
    With.frame = ObservationPeriod
    grid.update()
    assert(grid.visibilityReads == 2 * reads && grid.creepReads == 2 && grid.getUnchecked(50))
  }
  check("Tile creep accessors use the grid, including safe out-of-bounds access") {
    val unsafeField = classOf[sun.misc.Unsafe].getDeclaredField("theUnsafe")
    unsafeField.setAccessible(true)
    val unsafe = unsafeField.get(null).asInstanceOf[sun.misc.Unsafe]
    With.grids = unsafe.allocateInstance(classOf[Grids]).asInstanceOf[Grids]
    val grid = new Fixture()
    val field = classOf[Grids].getDeclaredField("creep")
    field.setAccessible(true)
    field.set(With.grids, grid)
    grid.set(3, 4, true)
    assert(Tile(3, 4).creep && Tile(3, 4).creepUnchecked)
    assert(!Tile(4, 4).creep && !Tile(-1, 4).creep && !Tile(With.mapTileWidth, 4).creep)
  }

  // Independent slow reference: repeatedly add one eligible tile, favoring the
  // largest number of creep neighbors, as in OpenBW's completed initial growth.
  def reference(source: InitialBuilding, width: Int, height: Int, terrain: Array[Boolean], occupied: Array[Boolean]): Array[Boolean] = {
    val out = new Array[Boolean](width * height)
    for (y <- Math.max(0, source.top) until Math.min(height, source.top + source.unitType.tileHeight);
         x <- Math.max(0, source.left) until Math.min(width, source.left + source.unitType.tileWidth)) {
      if (terrain(x + y * width)) out(x + y * width) = true
    }
    if (source.spreads) {
      var changed = true
      while (changed) {
        var best, bestNeighbors = -1
        for (y <- 0 until height; x <- 0 until width) {
          val i = x + y * width
          val dx = x * 32 + 16 - source.pixelX
          val dy = y * 32 + 16 - source.pixelY
          if (!out(i) && terrain(i) && !occupied(i) && 25 * dx * dx + 64 * dy * dy <= 25 * 320 * 320) {
            val neighbors = (for (ny <- Math.max(0, y - 1) to Math.min(height - 1, y + 1);
                                  nx <- Math.max(0, x - 1) to Math.min(width - 1, x + 1)) yield out(nx + ny * width)).count(identity)
            if (neighbors > 0 && neighbors > bestNeighbors) { best = i; bestNeighbors = neighbors }
          }
        }
        changed = best >= 0
        if (changed) out(best) = true
      }
    }
    out
  }
  check("flood-fill matches stepwise growth for every provider and random terrain/blockers") {
    val random = new scala.util.Random(93241)
    for (unitType <- Seq(UnitType.Zerg_Hatchery, UnitType.Zerg_Lair, UnitType.Zerg_Hive,
                        UnitType.Zerg_Creep_Colony, UnitType.Zerg_Sunken_Colony, UnitType.Zerg_Spore_Colony,
                        UnitType.Zerg_Spawning_Pool); trial <- 0 until 3) {
      val width = 32
      val height = 24
      val source = building(unitType, 14, 11)
      val terrain = Array.fill(width * height)(random.nextDouble() > 0.16)
      val occupied = Array.fill(width * height)(random.nextDouble() < 0.08)
      val actual = new Array[Boolean](width * height)
      GridCreep.seed(source, width, height, actual, occupied, i => terrain(i))
      assert(actual.sameElements(reference(source, width, height, terrain, occupied)), s"$unitType trial $trial")
    }
  }

  println(s"Creep grid: $passed passed")
}
