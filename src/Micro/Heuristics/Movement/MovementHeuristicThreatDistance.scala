package Micro.Heuristics.Movement

import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Pixels.Pixel
import Micro.Task.ExecutionState

object MovementHeuristicThreatDistance extends MovementHeuristic {
  
  override def evaluate(state: ExecutionState, candidate: Pixel): Double = {
  
    if (state.threats.isEmpty) return HeuristicMathMultiplicative.default
    
    state.threats
      .map(threat => threat.pixelDistanceFast(candidate) - threat.pixelRangeAgainstFromEdge(state.unit))
      .min
  }
}
