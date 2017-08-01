package Macro.Architecture.Heuristics

import Information.Geography.Types.Edge
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicDistanceFromIdealRange extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
    
    val targetDistance            = blueprint.preferredDistanceFromExit.get
    val candidatePixel            = candidate.topLeftPixel.add(16 * blueprint.widthTiles.get, 16 * blueprint.heightTiles.get)
    val zone                      = candidate.zone
    val exits: Iterable[Edge]     = if (zone.exit.isDefined) zone.exit else zone.edges
    
    // TODO: Once we add some better math tools use distance from line instead
    
    val totalDistance = exits
      .map(exit =>
        Math.abs(targetDistance - candidatePixel.pixelDistanceFast(exit.centerPixel)) +
        Math.abs(targetDistance - candidatePixel.pixelDistanceFast(exit.sidePixels.head)) +
        Math.abs(targetDistance - candidatePixel.pixelDistanceFast(exit.sidePixels.last)))
      .sum
  
    Math.max(128.0, totalDistance)
  }
}
