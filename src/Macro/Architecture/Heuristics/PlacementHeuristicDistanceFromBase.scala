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
        
        // Performance optimization. Town hall placements matter more so use the zone distance as well.
        if (blueprint.requireTownHallTile.get)
          totalDistance += from.zone.distancePixels(to.zone)
        totalDistance += from.pixelDistanceFast(to)
      })
      Math.max(128.0, totalDistance)
    }
  }
}
