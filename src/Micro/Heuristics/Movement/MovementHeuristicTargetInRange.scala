package Micro.Heuristics.Movement

import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Pixel
import Micro.Execution.ActionState

object MovementHeuristicTargetInRange extends MovementHeuristic {
  
  override def evaluate(state: ActionState, candidate: Pixel): Double = {
  
    if (state.toAttack.isEmpty) return HeuristicMathMultiplicative.default
    
    HeuristicMathMultiplicative.fromBoolean(
      state.toAttack.get.pixelDistanceSquared(candidate) <
      Math.pow(
        state.unit.pixelRangeAgainstFromEdge(state.toAttack.get) +
        state.unit.unitClass.radialHypotenuse +
        state.toAttack.get.unitClass.radialHypotenuse,
        2))
  }
}
