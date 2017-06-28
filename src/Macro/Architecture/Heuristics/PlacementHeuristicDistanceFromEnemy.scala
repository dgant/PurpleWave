package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.BuildingDescriptor
import Mathematics.Points.Tile

object PlacementHeuristicDistanceFromEnemy extends PlacementHeuristic {
  
  override def evaluate(building: BuildingDescriptor, candidate: Tile): Double = {
  
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
