package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicDistanceFromBase extends PlacementHeuristic {
  
  override def evaluate(building: Blueprint, candidate: Tile): Double = {
  
    if (With.geography.ourBases.isEmpty)
      With.geography.home.groundPixels(candidate)
    else
      With.geography.ourBases
        .map(_.townHallArea.midPixel)
        .map(basePixel =>
          Math.min(
            3 * basePixel.pixelDistanceFast(candidate.pixelCenter),
            basePixel.groundPixels(candidate))).sum
  }
}
