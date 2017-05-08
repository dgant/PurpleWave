package Micro.Heuristics.Targeting

import Micro.State.ExecutionState
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicHealth extends TargetHeuristic{
  
  override def evaluate(state: ExecutionState, candidate: UnitInfo): Double = {
    
    candidate.totalHealth
    
  }
  
}
