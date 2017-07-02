package Macro.Architecture.Heuristics

import Information.Geography.Types.Edge
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicDistanceFromExitRange extends PlacementHeuristic {
  
  override def evaluate(building: Blueprint, candidate: Tile): Double = {
    
    val targetDistance            = building.attackRange.getOrElse(32.0 * 6.0)
    val candidatePixel            = candidate.topLeftPixel.add(16 * building.width, 16 * building.height)
    val zone                      = candidate.zone
    val exits: Iterable[Edge] = if (zone.exit.isDefined) zone.exit else zone.edges
    
    // TODO: Once we add some better math tools use distance from line instead
    
    exits
      .map(exit =>
        Math.abs(targetDistance - candidatePixel.pixelDistanceFast(exit.centerPixel)) +
        Math.abs(targetDistance - candidatePixel.pixelDistanceFast(exit.sidePixels.head)) +
        Math.abs(targetDistance - candidatePixel.pixelDistanceFast(exit.sidePixels.last)))
      .sum
  }
}
