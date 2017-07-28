package Micro.Heuristics.Targeting

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicDelay extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    1.0 + unit.framesToGetInRange(candidate)
  }
}
