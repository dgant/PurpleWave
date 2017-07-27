package Micro.Heuristics.Targeting

import Mathematics.Heuristics.Heuristic
import Micro.Execution.ActionState
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

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
  
  def audit(unit: FriendlyUnitInfo): Seq[(UnitInfo, Double, Iterable[(Heuristic[ActionState, UnitInfo], Double)])] = {
    val output = unit.matchups.targets.map(target =>
      (target, EvaluateTargets.evaluate(unit.action, target), unit.action.targetProfile.weightedHeuristics.map(h =>
        (h.heuristic, h.weighMultiplicatively(unit.action, target))))).sortBy(_._2)
    return output
  }
}
