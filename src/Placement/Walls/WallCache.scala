package Placement.Walls

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import Mathematics.Shapes.{Box, Rectangle}
import Placement.Walls.WallSpans.{TerrainGas, TerrainHall}
import ProxyBwapi.UnitClasses.UnitClass

class WallCache {

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

    val perimeterUnwalkable           = zone.perimeter.view.filterNot(_.walkable)
    val tileExit                      = zone.exitOriginal.get.pixelCenter.tile
    val tileEntrance                  = zone.entranceOriginal.filterNot(zone.exitOriginal.contains).map(_.pixelCenter.tile).getOrElse(zone.heart)
    val perimeterTileExit             = zone.perimeter.minBy(tileExit.tileDistanceSquared)
    val perimeterTileEntrance         = zone.perimeter.minBy(tileEntrance.tileDistanceSquared)
    val perimeterEntranceExitDistance = perimeterTileEntrance.tileDistanceFast(perimeterTileExit)
    val perimeterRotation             = zone.perimeter.toVector.sortBy(zone.centroid.radiansTo)
    val boundaryStart                 = Maff.shortestItinerary(perimeterTileExit, perimeterTileEntrance, perimeterRotation).filterNot(_.walkable).toVector
    lazy val boundaryEndTerrain       = zone.perimeter.filterNot(_.walkable).filter(_.tileDistanceFast(perimeterTileExit) < perimeterEntranceExitDistance + 8) -- boundaryStart
    lazy val boundaryEndHall          = zone.bases.flatMap(_.townHallArea.tilesAtEdge)
    lazy val boundaryEndGas           = zone.bases.flatMap(_.gas.flatMap(_.tileArea.tilesAtEdge))
    def boundaryEnd                   = constraint.span match { case TerrainGas => boundaryEndGas; case TerrainHall => boundaryEndHall; case _ => boundaryEndTerrain.toVector }

    def tryPlace(tryBoundary: Seq[Tile], buildingsLeft: Seq[UnitClass], gapsLeft: Int): Boolean = {
      // TODO: Respect constraint.blocksUnit
      if (buildingsLeft.isEmpty) {
        return tryBoundary.exists(a => boundaryEnd.exists(_.tileDistanceManhattan(a) < gapsLeft + 1))
      }
      val building = buildingsLeft.head
      val width = building.tileWidthPlusAddon
      val height = building.tileHeight
      // For each tile in the boundary, try each way to place a building adjacent to it
      tryBoundary.exists(tryBoundaryTile =>
        (0 to gapsLeft).exists(gapsUsed =>
          Box(1 + 2 * gapsUsed + width, 1 + 2 * gapsUsed + height).exists(relative => {
            val buildingTile = tryBoundaryTile.subtract(width, height).add(relative).subtract(gapsUsed, gapsUsed)
            val buildingEndX = buildingTile.x + width
            val buildingEndY = buildingTile.y + height
            if ( ! With.grids.buildableW(width)(buildingTile)) {
              false
            } else if (wall.buildings.exists(existingBuilding => Maff.rectanglesIntersect(buildingTile.x, buildingTile.y, buildingEndX, buildingEndY,
              existingBuilding._1.x, existingBuilding._1.y,
              existingBuilding._1.x + existingBuilding._2.tileWidthPlusAddon, existingBuilding._1.y + existingBuilding._2.tileHeight))) {
              false
            } else if (buildingTile.zone.bases.exists(b => Maff.rectanglesIntersect(buildingTile.x, buildingTile.y, buildingEndX, buildingEndY,
              b.townHallArea.startInclusive.x, b.townHallArea.startInclusive.y,
              b.townHallArea.endExclusive.x, b.townHallArea.endExclusive.y))) {
              false
            } else if (Rectangle(width, height).map(buildingTile.add).exists( ! _.buildable)) {
              false
            } else {
              wall.buildings += ((buildingTile, building))
              val output = tryPlace(building.tileArea.tilesAtEdge.map(buildingTile.add), buildingsLeft.drop(1), gapsLeft - gapsUsed)
              if ( ! output) {
                wall.buildings.remove(wall.buildings.length - 1)
              }
              output
            }
          })))
    }

    while (constraintAvailable) {
      nextConstraint()
      val permutations = constraint.buildings.permutations.toVector.distinct
      if (permutations.exists(permutation => tryPlace(boundaryStart, permutation.view, constraint.gapTiles))) {
        return Some(wall)
      }
    }
    None
  }
}
