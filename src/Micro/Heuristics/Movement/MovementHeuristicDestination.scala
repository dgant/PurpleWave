package Micro.Heuristics.Movement
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Pixel
import Micro.Execution.ActionState

object MovementHeuristicDestination extends MovementHeuristic {
  
  override def evaluate(state: ActionState, candidate: Pixel): Double = {
    
    if (state.toTravel.isEmpty) return HeuristicMathMultiplicative.default
    
    val before = state.unit.pixelDistanceTravelling(state.unit.pixelCenter, state.toTravel.get)
    val after  = state.unit.pixelDistanceTravelling(candidate,              state.toTravel.get)
  
    before - after
  }
}
