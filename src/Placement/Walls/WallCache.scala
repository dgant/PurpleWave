package Placement.Walls

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Point, Tile}
import Mathematics.Shapes.{Rectangle, RoundedBox}
import Placement.Walls.WallSpans.{TerrainGas, TerrainHall}
import ProxyBwapi.UnitClasses.UnitClass

class WallCache {

  // Logging metrics
  var metricPermutations        = 0
  var metricTilesConsidered     = 0
  var metricUnbuildableTerrain  = 0
  var metricIntersectsPrevious  = 0
  var metricIntersectsHall      = 0
  var metricUnbuildableGranular = 0
  var metricFailedRecursively   = 0
  var metricGapTooNarrow        = 0
  var metricGapTooWide          = 0

  def generate(zone: Zone, constraints: Seq[WallConstraint]): Option[Wall] = {
    if (constraints.isEmpty) return None
    if (zone.exitOriginal.isEmpty) return None

    val wall            : Wall            = new Wall
    var constraint      : WallConstraint  = null
    var constraintIndex : Int             = 0
    var gapsLeft        : Int             = 0
    def constraintAvailable: Boolean = constraintIndex < constraints.length
    def nextConstraint(): Unit = {
      wall.buildings.clear()
      constraint = constraints(constraintIndex)
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
      case TerrainGas => zone.wallPerimeterGas;
      case TerrainHall => zone.wallPerimeterHall;
      case _ => zone.wallPerimeterExit }

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
        return true
      }
      val building = buildingsLeft.head
      val width = building.tileWidthPlusAddon
      val height = building.tileHeight
      // For each tile in the boundary, try each way to place a building adjacent to it
      horizon.exists(horizonTile => {
        metricTilesConsidered += 1
        // Cater-corner adjacency isn't zero-gap
        val boxWidth = width + 2
        val boxHeight = height + 2
        RoundedBox(boxWidth, boxHeight).exists(relative => {
          (0 to gapsLeft).exists(gapsUsed => {
            // TODO: If gapsUsed is zero enforce constraint.blocksUnit
            val buildingTile = horizonTile
              .subtract(width, height)
              .add(relative)
              .add(   if (relative.x == 0)            Point(-gapsUsed, 0)
                else  if (relative.y == 0)            Point(0,        -gapsUsed)
                else  if (relative.x == boxWidth - 1) Point(gapsUsed, 0)
                else                                  Point(0,        gapsUsed))
            val buildingEndX = buildingTile.x + width
            val buildingEndY = buildingTile.y + height
            if ( ! With.grids.buildableW(width)(buildingTile)) {
              metricUnbuildableTerrain += 1
              false
            } else if (wall.buildings.exists(existingBuilding => Maff.rectanglesIntersect(buildingTile.x, buildingTile.y, buildingEndX, buildingEndY,
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
            } else {
              wall.buildings += ((buildingTile, building))
              val output = tryPlace(building.tileArea.tilesAtEdge.map(buildingTile.add), buildingsLeft.drop(1), gapsLeft - gapsUsed)
              if (!output) {
                metricFailedRecursively += 1
                wall.buildings.remove(wall.buildings.length - 1)
              }
              output
            }
          })})})
    }

    while (constraintAvailable) {
      nextConstraint()
      val permutations = constraint.buildings.permutations.toVector.distinct
      metricPermutations += permutations.length
      if (permutations.exists(permutation => tryPlace(zone.wallPerimeterEntrance, permutation.view, constraint.gapTiles))) {
        return Some(wall)
      }
    }
    None
  }
}
