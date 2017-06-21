package Micro.Heuristics.Targeting

import Mathematics.Heuristics.HeuristicMathMultiplicative
import Micro.Execution.ActionState
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicInRange extends TargetHeuristic{
  
  override def evaluate(state: ActionState, candidate: UnitInfo): Double = {
    
    HeuristicMathMultiplicative.fromBoolean(state.unit.inRangeToAttackFast(candidate))
    
  }
  
}
