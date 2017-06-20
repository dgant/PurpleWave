package Micro.Heuristics.Movement

import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Pixel
import Micro.Execution.ExecutionState

object MovementHeuristicTargetValue extends MovementHeuristic {
  
  override def evaluate(state: ExecutionState, candidate: Pixel): Double = {
    
    if ( state.targets.isEmpty) return HeuristicMathMultiplicative.default
    
    val targetValues = state.targetValues
      .filter(pair =>
        pair._1.pixelDistanceFast(candidate) < state.unit.pixelRangeAgainstFromCenter(pair._1))
      .map(_._2)
      
    if (targetValues.isEmpty) return HeuristicMathMultiplicative.default
    
    // A Supply Depot is worth about 4E-5.
    // This multiplier will require re-tuning if target heuristics change much.
    val output = 100000 * targetValues.max
    
    output
  }
}
