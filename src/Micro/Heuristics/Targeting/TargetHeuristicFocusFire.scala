package Micro.Heuristics.Targeting

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicFocusFire extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    24.0 * Math.min(
      candidate.matchups.dpfReceivingDiffused,
      candidate.matchups.dpfReceivingDiffused * candidate.totalHealth / 12.0)
  }
}
