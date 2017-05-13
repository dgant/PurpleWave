package Micro.Heuristics.Targeting

import Mathematics.Heuristics.HeuristicMathMultiplicative
import Micro.Task.ExecutionState
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicInRange extends TargetHeuristic{
  
  override def evaluate(state: ExecutionState, candidate: UnitInfo): Double = {
    
    HeuristicMathMultiplicative.fromBoolean(state.unit.inRangeToAttackFast(candidate))
    
  }
  
}
