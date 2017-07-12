package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicDistanceFromBase extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
  
    if (With.geography.ourBases.isEmpty)
      With.geography.home.pixelCenter.pixelDistanceFast(candidate.pixelCenter)
    else {
      var totalDistance = 0.0
      With.geography.ourBases.foreach(base => {
        val from  = base.heart.pixelCenter
        val to    = candidate.pixelCenter
        
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
}
