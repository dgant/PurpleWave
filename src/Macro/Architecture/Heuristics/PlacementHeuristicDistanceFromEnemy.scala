package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicDistanceFromEnemy extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
  
    val enemyBases =
    if (With.geography.enemyBases.isEmpty)
      With.intelligence.leastScoutedBases.filter(_.isStartLocation).take(1)
    else
      With.geography.enemyBases
    
    var totalDistance = 0.0
    enemyBases.foreach(base => {
      val from = base.townHallArea.midPixel
      val to = candidate.pixelCenter
  
      // Performance optimization.
      // We want ground distance for expansions, but that's too luxurious for ordinary buildings
      if (blueprint.townHall)
        totalDistance += from.groundPixels(to)
      else
        totalDistance += from.pixelDistanceFast(to)
      
      })
    totalDistance
  }
}
