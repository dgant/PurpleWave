package Micro.Heuristics.Targeting

import Mathematics.Heuristics.Heuristic
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object EvaluateTargets {
  
  def best(unit: FriendlyUnitInfo, targets: Iterable[UnitInfo]): Option[UnitInfo] = {
    
    if (targets.isEmpty)
      None
    else
      Some(targets.maxBy(target => evaluate(unit, target)))
  }
  
  def evaluate(unit: FriendlyUnitInfo, target: UnitInfo): Double = {
    unit.agent.targetProfile.weightedHeuristics
      .map(_.weighMultiplicatively(unit, target))
      .product
  }
  
  def audit(unit: FriendlyUnitInfo): Seq[(UnitInfo, Double, Iterable[(Heuristic[FriendlyUnitInfo, UnitInfo], Double, Double)])] = {
    val output = unit.matchups.targets.map(target =>
      (
        target,
        EvaluateTargets.evaluate(unit, target),
        unit.agent.targetProfile.weightedHeuristics.map(h =>
        (
          h.heuristic,
          h.weighMultiplicatively(unit, target),
          h.heuristic.evaluate(unit, target)
        ))
      )
    ).sortBy(_._2)
    output
  }
}
