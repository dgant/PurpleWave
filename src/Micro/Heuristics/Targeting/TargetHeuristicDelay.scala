package Micro.Heuristics.Targeting

import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicDelay extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
  
    val chasingPenalty =
      if (unit.topSpeedChasing > 0)
        PurpleMath.nanToInfinity(candidate.topSpeedChasing / unit.topSpeedChasing)
      else
        100000.0 //Not infinity, because we still want chasingPenalty * 0 == 0
      
    1.0 + chasingPenalty * unit.framesToGetInRange(candidate)
  }
}
