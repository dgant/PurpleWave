package Placement.Walls

import Information.Geography.Types.Zone
import Mathematics.Maff
import Mathematics.Points.Tile
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
    def nextConstraint(): Unit = {
      wall.buildings.clear()
      constraint = constraints(constraintIndex)
      constraintIndex += 1
      gapsLeft = constraint.gapTiles
    }
    nextConstraint()

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
    val tileEntrance                  = zone.entranceOriginal.map(_.pixelCenter.tile).getOrElse(zone.heart)
    val perimeterTileExit             = zone.perimeter.minBy(tileExit.tileDistanceSquared)
    val perimeterTileEntrance         = zone.perimeter.minBy(tileEntrance.tileDistanceSquared)
    val perimeterEntranceExitDistance = perimeterTileEntrance.tileDistanceFast(perimeterTileExit)
    val perimeterRotation             = zone.perimeter.toVector.sortBy(zone.centroid.radiansTo)
    val boundaryStart                 = Maff.shortestItinerary(perimeterTileExit, perimeterTileEntrance, perimeterRotation).filterNot(_.walkable)
    lazy val boundaryEndTerrain       = zone.perimeter.filterNot(_.walkable).filter(_.tileDistanceFast(perimeterTileExit) < perimeterEntranceExitDistance + 8) -- boundaryStart
    lazy val boundaryEndHall          = zone.bases.flatMap(_.townHallArea.tilesAtEdge)
    lazy val boundaryEndGas           = zone.bases.flatMap(_.gas.flatMap(_.tileArea.tilesAtEdge))
    val boundaryEnd                   = constraint.span match { case TerrainGas => boundaryEndGas; case TerrainHall => boundaryEndHall; case _ => boundaryEndTerrain }

    def tryPlace(adjacentTo: Seq[Tile], buildings: Seq[UnitClass], gapsLeft: Int): Boolean = {
      false
    }

    val success = tryPlace(boundaryStart, constraint.buildings, constraint.gapTiles)

    if (success) Some(wall) else None
  }
}
