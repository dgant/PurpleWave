package Micro.Heuristics.Movement

import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Pixel
import Micro.Execution.ActionState

object MovementHeuristicOrigin extends MovementHeuristic {
  
  override def evaluate(state: ActionState, candidate: Pixel): Double = {
    
    val zone = state.unit.pixelCenter.zone
    
    if (zone.bases.exists(_.owner.isUs)) return HeuristicMathMultiplicative.default
  
    val candidateDistance = state.unit.tileIncludingCenter.pixelCenter.pixelDistanceFast(candidate)
    
    if (candidateDistance <= 0) return HeuristicMathMultiplicative.default
    
    val before = state.unit.pixelDistanceTravelling(state.unit.pixelCenter, state.origin)
    val after  = state.unit.pixelDistanceTravelling(candidate,              state.origin)
    
    (before - after) / candidateDistance
  }
}
