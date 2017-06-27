package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.BuildingDescriptor
import Mathematics.Points.Tile

object PlacementHeuristicEnemy extends PlacementHeuristic {
  
  override def evaluate(state: BuildingDescriptor, candidate: Tile): Double = {
  
    val enemyBases =
    if (With.geography.enemyBases.isEmpty)
      With.intelligence.leastScoutedBases.filter(_.isStartLocation).take(1)
    else
      With.geography.enemyBases
    
    enemyBases
      .map(_.townHallArea.midPixel)
      .map(basePixel =>
        Math.min(
          3 * basePixel.pixelDistanceFast(candidate.pixelCenter),
          basePixel.groundPixels(candidate)))
      .min
  }
}
