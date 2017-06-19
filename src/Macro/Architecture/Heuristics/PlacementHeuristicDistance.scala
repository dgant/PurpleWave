package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.BuildingDescriptor
import Mathematics.Points.Tile

object PlacementHeuristicDistance extends PlacementHeuristic {
  
  override def evaluate(state: BuildingDescriptor, candidate: Tile): Double = {
  
    if (With.geography.ourBases.isEmpty)
      With.paths.groundPixels(candidate, With.geography.home)
    else
      With.geography.ourBases.map(base =>With.paths.groundPixels(candidate, base.townHallArea.midpoint)).sum
  }
  
}
