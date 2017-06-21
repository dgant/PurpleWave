package Micro.Heuristics.Targeting

import Micro.Execution.ActionState
import ProxyBwapi.UnitInfo.UnitInfo

object EvaluateTargets {
  
  def best(state: ActionState, targets: Iterable[UnitInfo]): Option[UnitInfo] = {
    
    if (targets.isEmpty)
      None
    else
      Some(targets.maxBy(target => evaluate(state, target)))
  }
  
  def evaluate(state: ActionState, target: UnitInfo): Double = {
    state.targetProfile.weightedHeuristics
      .map(_.weighMultiplicatively(state, target))
      .product
  }
}
