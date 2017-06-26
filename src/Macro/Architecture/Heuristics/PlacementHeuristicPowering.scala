package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.BuildingDescriptor
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

object PlacementHeuristicPowering extends PlacementHeuristic {
  
  override def evaluate(state: BuildingDescriptor, candidate: Tile): Double = {
    
    if ( ! state.powers) return HeuristicMathMultiplicative.default
    
    With.grids.psi2x2and3x2.psiPoints
      .map(candidate.add)
      .count(tile =>
        tile.valid
        && With.grids.buildable.get(tile)
        && ! With.grids.psi2x2and3x2.get(tile))
  }
}
