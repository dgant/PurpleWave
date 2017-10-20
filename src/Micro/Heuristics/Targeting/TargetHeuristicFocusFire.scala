package Micro.Heuristics.Targeting

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicFocusFire extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    
    val doomed = candidate.matchups.threatsViolentInRange.map(_.damageOnNextHitAgainst(candidate)).sum > candidate.totalHealth
    
    if (doomed) return 1.0
    
    24.0 * candidate.matchups.dpfReceivingCurrently
  }
  
}
