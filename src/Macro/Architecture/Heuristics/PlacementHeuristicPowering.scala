package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.Blueprint
import Placement.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile
import Utilities.Time.Forever

object PlacementHeuristicPowering extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
    
    if ( ! blueprint.powers.get) return HeuristicMathMultiplicative.default
    
    With.grids.psi2Height.psiPoints
      .count(point => {
        val tile = candidate.add(point)
        val i = tile.i
        (tile.valid
          && With.grids.buildable.getUnchecked(i)
          && ! With.grids.psi2Height.isSetUnchecked(i)
          && With.architecture.powered2Height.getUnchecked(i) >= Forever())
      })
  }
}
