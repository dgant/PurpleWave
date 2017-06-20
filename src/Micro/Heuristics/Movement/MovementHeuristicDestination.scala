package Micro.Heuristics.Movement
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Pixel
import Micro.Execution.ExecutionState

object MovementHeuristicDestination extends MovementHeuristic {
  
  override def evaluate(state: ExecutionState, candidate: Pixel): Double = {
    
    if (state.toTravel.isEmpty) return HeuristicMathMultiplicative.default
    
    val candidateDistance = state.unit.tileIncludingCenter.pixelCenter.pixelDistanceFast(candidate)
  
    if (candidateDistance <= 0) return HeuristicMathMultiplicative.default
  
    val before = state.unit.pixelDistanceTravelling(state.unit.pixelCenter, state.toTravel.get)
    val after  = state.unit.pixelDistanceTravelling(candidate,              state.toTravel.get)
  
    (before - after) / candidateDistance
  }
}
