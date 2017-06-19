package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.BuildingDescriptor
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile
import Mathematics.Shapes.PylonRadius

object PlacementHeuristicPowering extends PlacementHeuristic {
  
  override def evaluate(state: BuildingDescriptor, candidate: Tile): Double = {
    
    if ( ! state.powers) return HeuristicMathMultiplicative.default
    
    // At time of writing, PylonRadius may have incorrect math, but is still a decent metric
    PylonRadius.points
      .map(candidate.add)
      .count(tile =>
        With.grids.buildable.get(tile)
        && ! With.grids.psi2x2and3x2.get(tile))
  }
}
