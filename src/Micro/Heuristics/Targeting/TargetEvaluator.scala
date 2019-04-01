package Micro.Heuristics.Targeting

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

trait TargetEvaluator {
  def best(attacker: FriendlyUnitInfo, targets: Iterable[UnitInfo]): Option[UnitInfo]
  def evaluate(attacker: FriendlyUnitInfo, target: UnitInfo): Double
}
