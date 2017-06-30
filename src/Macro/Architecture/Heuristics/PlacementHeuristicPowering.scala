package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

object PlacementHeuristicPowering extends PlacementHeuristic {
  
  override def evaluate(building: Blueprint, candidate: Tile): Double = {
    
    if ( ! building.powers) return HeuristicMathMultiplicative.default
    
    With.grids.psi2Height.psiPoints
      .map(candidate.add)
      .count(tile =>
        tile.valid
        && With.grids.buildable.get(tile)
        && ! With.grids.psi2Height.get(tile)
        && ! With.architecture.powered2Height.contains(tile))
  }
}
