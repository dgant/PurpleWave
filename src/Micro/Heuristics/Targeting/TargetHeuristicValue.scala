package Micro.Heuristics.Targeting

import Micro.Execution.ActionState
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicValue extends TargetHeuristic{
  
  override def evaluate(state: ActionState, candidate: UnitInfo): Double = {
  
    candidate.subjectiveValue
    
  }
}
