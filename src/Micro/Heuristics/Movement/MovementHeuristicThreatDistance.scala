package Micro.Heuristics.Movement

import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Pixel
import Micro.Execution.ActionState

object MovementHeuristicThreatDistance extends MovementHeuristic {
  
  override def evaluate(state: ActionState, candidate: Pixel): Double = {
  
    if (state.threats.isEmpty) return HeuristicMathMultiplicative.default
    
    state.threats
      .map(threat => threat.pixelDistanceFast(candidate) - threat.pixelRangeAgainstFromEdge(state.unit))
      .min
  }
}
