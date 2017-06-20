package Micro.Heuristics.Targeting

import Micro.Execution.ExecutionState
import ProxyBwapi.UnitInfo.UnitInfo

object EvaluateTargets {
  
  def best(state: ExecutionState, targets: Iterable[UnitInfo]): Option[UnitInfo] = {
    
    if (targets.isEmpty)
      None
    else
      Some(targets.maxBy(target => evaluate(state, target)))
  }
  
  def evaluate(state: ExecutionState, target: UnitInfo): Double = {
    state.targetProfile.weightedHeuristics
      .map(_.weighMultiplicatively(state, target))
      .product
  }
}
