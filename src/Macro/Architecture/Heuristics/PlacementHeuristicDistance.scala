package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.BuildingDescriptor
import Mathematics.Points.Tile

object PlacementHeuristicDistance extends PlacementHeuristic {
  
  override def evaluate(state: BuildingDescriptor, candidate: Tile): Double = {
  
    if (With.geography.ourBases.isEmpty)
      With.geography.home.groundPixels(candidate)
    else
      With.geography.ourBases.map(_.townHallArea.midpoint.groundPixels(candidate)).sum
  }
}
