package Micro.Heuristics.Targeting

import Micro.Task.ExecutionState
import ProxyBwapi.UnitInfo.UnitInfo

object EvaluateTargets {
  
  def best(state:ExecutionState, candidates:Iterable[UnitInfo]):Option[UnitInfo] = {
    
    if (candidates.isEmpty) return None
    
    Some(candidates.maxBy(candidate => state.targetProfile.weightedHeuristics.map(_.weighMultiplicatively(state, candidate)).product))
  }
}
