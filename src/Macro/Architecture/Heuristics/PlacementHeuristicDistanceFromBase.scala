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
        val from  = base.townHallArea.midPixel
        val to    = candidate.pixelCenter
        
        var baseDistance = 0.0
        
        // Performance optimization. Town hall placements matter more so use the zone distance as well.
        if (blueprint.requireTownHallTile.get) {
          baseDistance += from.zone.distancePixels(to.zone)
        }
        baseDistance += from.pixelDistanceFast(to)
        if ( ! base.townHall.exists(_.complete)) {
          baseDistance *= 0.5
        }
        
        totalDistance += baseDistance
      })
      Math.max(128.0, totalDistance)
    }
  }
}
