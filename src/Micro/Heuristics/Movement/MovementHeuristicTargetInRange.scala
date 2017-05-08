package Micro.Heuristics.Movement

import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Pixels.Pixel
import Micro.State.ExecutionState

object MovementHeuristicTargetInRange extends MovementHeuristic {
  
  override def evaluate(state: ExecutionState, candidate: Pixel): Double = {
  
    if (state.toAttack.isEmpty) return HeuristicMathMultiplicative.default
    
    HeuristicMathMultiplicative.fromBoolean(
      state.toAttack.get.pixelDistanceSquared(candidate) <
      Math.pow(
        state.unit.pixelRangeAgainst(state.toAttack.get) +
        state.unit.unitClass.radialHypotenuse +
        state.toAttack.get.unitClass.radialHypotenuse,
        2))
  }
}
