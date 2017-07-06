package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicDistanceFromBase extends PlacementHeuristic {
  
  override def evaluate(building: Blueprint, candidate: Tile): Double = {
  
    if (With.geography.ourBases.isEmpty)
      With.geography.home.pixelCenter.pixelDistanceFast(candidate.pixelCenter)
    else {
      var totalDistance = 0.0
      With.geography.ourBases.foreach(base => totalDistance += base.townHallArea.midPixel.pixelDistanceFast(candidate.pixelCenter))
      totalDistance
    }
  }
}
