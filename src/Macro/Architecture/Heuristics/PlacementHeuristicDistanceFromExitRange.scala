package Macro.Architecture.Heuristics

import Information.Geography.Types.Edge
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicDistanceFromExitRange extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
    
    val targetDistance            = blueprint.attackRange.getOrElse(32.0 * 6.0)
    val candidatePixel            = candidate.topLeftPixel.add(16 * blueprint.width, 16 * blueprint.height)
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
