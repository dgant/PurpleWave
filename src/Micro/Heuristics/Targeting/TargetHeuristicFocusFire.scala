package Micro.Heuristics.Targeting

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicFocusFire extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    
    val doomed = candidate.totalHealth <=
      candidate.matchups.threatsInRange
        .filter(threat => threat != unit && threat.lastTarget.contains(candidate))
        .map(_.damageOnNextHitAgainst(candidate))
        .sum
    
    if (doomed) return 1.0
    
    24.0 * candidate.matchups.dpfReceivingCurrently
  }
  
}
