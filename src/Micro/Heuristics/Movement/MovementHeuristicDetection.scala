package Micro.Heuristics.Movement

import Lifecycle.With
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Pixel
import Micro.Execution.ActionState

object MovementHeuristicDetection extends MovementHeuristic {
  
  override def evaluate(state: ActionState, candidate: Pixel): Double = {
  
    if ( ! state.unit.cloaked) return HeuristicMathMultiplicative.default
    
    HeuristicMathMultiplicative.fromBoolean(With.grids.enemyDetection.get(candidate.tileIncluding))
  }
}
