package Micro.Heuristics.Movement

import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Pixel
import Micro.Execution.ExecutionState

object MovementHeuristicOrigin extends MovementHeuristic {
  
  override def evaluate(state: ExecutionState, candidate: Pixel): Double = {
    
    val zone = state.unit.tileIncludingCenter.zone
    if (zone.bases.nonEmpty && zone.owner.isUs) return HeuristicMathMultiplicative.default
  
    val candidateDistance = state.unit.tileIncludingCenter.pixelCenter.pixelDistanceFast(candidate)
    
    if (candidateDistance <= 0) return HeuristicMathMultiplicative.default
    
    val before = state.unit.pixelDistanceTravelling(state.unit.pixelCenter, state.origin)
    val after  = state.unit.pixelDistanceTravelling(candidate,              state.origin)
    
    (before - after) / candidateDistance
  }
}
