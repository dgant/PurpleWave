package Micro.Heuristics.Movement

import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Pixels.Pixel
import Micro.State.ExecutionState

object MovementHeuristicThreatDistance extends MovementHeuristic {
  
  override def evaluate(state: ExecutionState, candidate: Pixel): Double = {
  
    if (state.threats.isEmpty) return HeuristicMathMultiplicative.default
    
    state.threats.map(threat => threat.dpsAgainst(state.unit) * threat.pixelDistanceFast(candidate) - threat.pixelRangeAgainst(state.unit)).min / 20.0
  }
}
