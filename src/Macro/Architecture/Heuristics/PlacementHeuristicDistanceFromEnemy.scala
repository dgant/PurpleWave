package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicDistanceFromEnemy extends PlacementHeuristic {
  
  override def evaluate(building: Blueprint, candidate: Tile): Double = {
  
    val enemyBases =
    if (With.geography.enemyBases.isEmpty)
      With.intelligence.leastScoutedBases.filter(_.isStartLocation).take(1)
    else
      With.geography.enemyBases
    
    var totalDistance = 0.0
    enemyBases.foreach(base => totalDistance += base.townHallArea.midPixel.pixelDistanceFast(candidate.pixelCenter))
    totalDistance
  }
}
