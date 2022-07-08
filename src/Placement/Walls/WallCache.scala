package Placement.Walls

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Types.{Edge, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Point, Tile}
import Mathematics.Shapes.{Corners, Rectangle, RoundedBox}
import Placement.Generation.TileGeneratorRectangularSweep
import Placement.Walls.WallSpans.{TerrainGas, TerrainHall}
import ProxyBwapi.Races.Neutral
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable.ArrayBuffer

class WallCache(zone: Zone, exit: Edge, entrance: Tile, constraints: Seq[WallConstraint], filler: Seq[UnitClass]) {

  // Logging metrics
  var metricPermutations        = 0
  var metricTilesConsidered     = 0
  var metricUnbuildableTerrain  = 0
  var metricIntersectsPrevious  = 0
  var metricIntersectsHall      = 0
  var metricUnbuildableGranular = 0
  var metricInsufficientlyTight = 0
  var metricWrongZone           = 0
  var metricFailedRecursively   = 0
  var metricGapTooNarrow        = 0
  var metricGapTooWide          = 0
  var metricAcceptableWalls     = 0

  val wallScores = new ArrayBuffer[(Wall, Double)]()
  var bestWall: Option[Wall] = None

  def generate(): Option[Wall] = {
    placeRequiredBuildings()
    if (bestWall.isEmpty) return None
    placeFiller()
    bestWall
  }

  def placeRequiredBuildings(): Option[Wall] = {
    if (constraints.isEmpty) return None
    if (zone.exitOriginal.isEmpty) return None

    val incompleteWall  : Wall  = new Wall
    var constraintIndex : Int   = 0
    var gapsLeft        : Int   = 0

    def constraintAvailable: Boolean = constraintIndex < constraints.length

    def constraint: WallConstraint = incompleteWall.constraint

    def nextConstraint(): Unit = {
      incompleteWall.buildings.clear()
      incompleteWall.constraint = constraints(constraintIndex)
      constraintIndex += 1
      gapsLeft = constraint.gapTiles
    }

    // Overall plan:
    // - Identify starting boundary
    // - Identify ending boundaries
    //   - Terrain-Terrain
    //   - Terrain-Gas
    //   - Terrain-Hall
    // For each constraint:
    // - For each starting boundary:
    //  - For each ending boundary:
    //    - If this line contains any other starting/ending boundaries, skip
    //    - If this line's max width exceeds the max width of our buildings + gap size, skip
    //    - If this line's max height exceeds the max height of our buildings + gap size, skip
    //    - Fill queue with buildings to place
    //    - For each place we could place the first building adjacent to the first tile: (LATER: Select cleverly to fill gaps)
    //      - Try placing each building adjacent to it or using our gap budget
    def boundaryEnd = constraint.span match {
      case TerrainGas   => zone.wallPerimeterGas;
      case TerrainHall  => zone.wallPerimeterHall;
      case _            => zone.wallPerimeterExit
    }

    def tryPlace(horizon: Seq[Tile], buildingsLeft: Seq[UnitClass], gapsLeft: Int): Boolean = {
      // TODO: If gapsLeft is zero enforce constraint.blocksUnit
      if (buildingsLeft.isEmpty) {
        if (horizon.forall(t => boundaryEnd.forall(_.tileDistanceManhattan(t) > 1 + gapsLeft))) {
          metricGapTooWide += 1
          return false
        }
        // Use Chebyshev distance to check gap size because cater-corner diagonals are not acceptable
        if (horizon.exists(t => boundaryEnd.exists(_.tileDistanceChebyshev(t) <= gapsLeft))) {
          metricGapTooNarrow += 1
          return false
        }
        // TODO: Test power
        wallScores += ((new Wall(incompleteWall), scoreWall(incompleteWall)))
        return true
      }
      val building  = buildingsLeft.head
      val width     = building.tileWidthPlusAddon
      val height    = building.tileHeight

      /*
      For each tile in the boundary,
        for each way to place a tile adjacent to it,
          for each gap size still available,
            Try placing the next building there,
            then repeat recursively.
            If we're out of buildings to place,
            and the whole arrangement is legal,
            publish it.

      Places around the target tile to try at each gap size:
         1111
         0000
       10----01
       10----01
       10---+01
         0000
         1111
       */
      0 < horizon.count(horizonTile => {
        metricTilesConsidered += 1
        val boxWidth  = width   + 2
        val boxHeight = height  + 2
        0 < RoundedBox(boxWidth, boxHeight).count(relative =>
          0 < (0 to gapsLeft).count(gapsUsed => {
            val gapOffset =
                    if (relative.x == 0)            Point(- gapsUsed,   0)
              else  if (relative.y == 0)            Point(  0,        - gapsUsed)
              else  if (relative.x == boxWidth - 1) Point(  gapsUsed,   0)
              else                                  Point(  0,          gapsUsed)
            val buildingTile = horizonTile
              .subtract(width, height)
              .add(relative)
              .add(gapOffset)
            val buildingEndX = buildingTile.x + width
            val buildingEndY = buildingTile.y + height
            lazy val (alleyLeft, alleyRight, alleyUp, alleyDown) = incompleteWall.buildings.lastOption.map(b => {
              val lastBuilding  = b._2
              val lastTile      = b._1
              val tileGapLeft   = buildingTile.x  - lastTile.x      - lastBuilding.tileWidthPlusAddon
              val tileGapRight  = lastTile.x      - buildingTile.x  - width
              val tileGapUp     = buildingTile.y  - lastTile.y      - lastBuilding.tileHeight
              val tileGapDown   = lastTile.y      - buildingTile.y  - height
              val alleyLeft     = if (tileGapLeft   == 0) lastBuilding.marginRight  + building.marginLeft   else -1
              val alleyRight    = if (tileGapRight  == 0) lastBuilding.marginLeft   + building.marginRight  else -1
              val alleyUp       = if (tileGapUp     == 0) lastBuilding.marginDown   + building.marginUp     else -1
              val alleyDown     = if (tileGapDown   == 0) lastBuilding.marginUp     + building.marginDown   else -1
              (alleyLeft, alleyRight, alleyUp, alleyDown)
            }).getOrElse((-1, -1, -1, -1))
            if (!With.grids.buildableW(width)(buildingTile)) {
              metricUnbuildableTerrain += 1
              false
            } else if (incompleteWall.buildings.exists(existingBuilding => Maff.rectanglesIntersect(buildingTile.x, buildingTile.y, buildingEndX, buildingEndY,
              existingBuilding._1.x, existingBuilding._1.y,
              existingBuilding._1.x + existingBuilding._2.tileWidthPlusAddon, existingBuilding._1.y + existingBuilding._2.tileHeight))) {
              metricIntersectsPrevious += 1
              false
            } else if (buildingTile.zone.bases.exists(b => Maff.rectanglesIntersect(buildingTile.x, buildingTile.y, buildingEndX, buildingEndY,
              b.townHallArea.startInclusive.x, b.townHallArea.startInclusive.y,
              b.townHallArea.endExclusive.x, b.townHallArea.endExclusive.y))) {
              metricIntersectsHall += 1
              false
            } else if (Rectangle(width, height).map(buildingTile.add).exists(!_.buildable)) {
              metricUnbuildableGranular += 1
              false
            } else if (incompleteWall.buildings.isEmpty && Corners(width, height).map(buildingTile.add).forall(_.zone != zone)) {
              metricWrongZone += 1
              false
            } else if (gapsUsed == 0 && (alleyLeft >= constraint.blocksUnit.width || alleyRight >= constraint.blocksUnit.width || alleyUp >= constraint.blocksUnit.height || alleyDown >= constraint.blocksUnit.height)) {
              metricInsufficientlyTight += 1
              false
            } else {
              // Apply changes
              val gapBefore = incompleteWall.gap
              incompleteWall.buildings += ((buildingTile, building))
              if (gapBefore.isEmpty && gapsUsed > 0) {
                incompleteWall.gap = Some(horizonTile.add(gapOffset.direction.x, gapOffset.direction.y))
              }

              // Test changes
              val output = tryPlace(building.tileArea.tilesAtEdge.map(buildingTile.add), buildingsLeft.drop(1), gapsLeft - gapsUsed)
              if ( ! output) {
                metricFailedRecursively += 1
              }

              // Revert changes
              incompleteWall.gap = gapBefore
              incompleteWall.buildings.remove(incompleteWall.buildings.length - 1)
              output
            }
          }))})
    }

    while (constraintAvailable && bestWall.isEmpty) {
      nextConstraint()
      val permutations = constraint.buildings.permutations.toVector.distinct
      metricPermutations += permutations.length
      permutations.foreach(tryPlace(zone.wallPerimeterEntrance, _, constraint.gapTiles))
      metricAcceptableWalls = wallScores.length
      bestWall = Maff.minBy(wallScores)(_._2).map(_._1)
    }
    bestWall
  }

  def scoreWall(wall: Wall): Double = {
    if (wall.buildings.isEmpty) return 0.0
    val endTiles = wall.buildings.map(p => p._1.add(p._2.tileWidth, p._2.tileHeight))
    val xMin = wall.buildings.map(_._1.x).min
    val yMin = wall.buildings.map(_._1.y).min
    val xMax = endTiles.map(_.x).max
    val yMax = endTiles.map(_.y).max
    val scoreArea = Maff.broodWarDistance(xMin, yMin, xMax, yMax)
    scoreArea
  }

  def placeFiller(): Unit = {
    if (filler.isEmpty) return
    if (bestWall.get.gap.isDefined) {
      val gap = bestWall.get.gap.get
      val hallwayWalls      = bestWall.get.buildings.flatMap(p => p._2.tileArea.add(p._1).tiles).toSet
      val hallwayProfile    = new PathfindProfile(gap, Some(entrance), employGroundDist = true, alsoUnwalkable = hallwayWalls)
      val hallwayPath       = hallwayProfile.find.tiles
      bestWall.get.hallway  = hallwayPath.getOrElse(Seq.empty)
      if (bestWall.get.hallway.isEmpty) return // We can't safely fill without preserving a path
    }
    var i = 0
    var lastFiller = Neutral.PsiDisruptor
    var lastFilled = true
    while (i < filler.length) {
      val fill = filler(i)
      // Don't try and fail to fill the same thing multiple times
      if (lastFilled || fill != lastFiller) {
        lastFilled = tryFill(fill)
      }
      lastFiller = fill
      i += 1
    }
  }

  def tryFill(fill: UnitClass): Boolean = {
    val wall = bestWall.get
    val generator = new TileGeneratorRectangularSweep(exit.pixelCenter.tile, zone.boundary.startInclusive, zone.boundary.endExclusive, exit.pixelCenter.directionTo(zone.heart.center))
    while (generator.hasNext) {
      val tile = generator.next()
      if (tile.zone == zone
        && With.grids.buildableW(fill.tileWidthPlusAddon)(tile)
        && ! wall.hallway.contains(tile)
        && ! wall.buildings.exists(b =>
          64 +  zone.heart.center.pixelDistanceChebyshev(b._1.topLeftPixel.add(16 * b._2.tileWidthPlusAddon, 16 * b._2.tileHeight))
          <     zone.heart.center.pixelDistanceChebyshev(tile.topLeftPixel.add(16 * fill.tileWidthPlusAddon, 16 * fill.tileHeight)))
        && ! wall.buildings.exists(b => Maff.rectanglesIntersect(
          b._1.x, b._1.y, b._1.x + b._2.tileWidthPlusAddon, b._1.y + b._2.tileHeight,
          tile.x, tile.y, tile.x + fill.tileWidthPlusAddon, tile.y + fill.tileHeight))) {
        // TODO: Test power
        wall.buildings += ((tile, fill))
        return true
      }
    }
    false
  }
}