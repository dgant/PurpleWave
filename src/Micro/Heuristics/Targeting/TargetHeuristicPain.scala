package Micro.Heuristics.Targeting

import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.PurpleMath
import Micro.Decisions.MicroValue
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicPain extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
  
    val threats = unit.matchups.threats.take(20)
    
    if (threats.isEmpty) return HeuristicMathMultiplicative.default
    val distance        = unit.pixelDistanceEdge(candidate)
    val framesToKill    = candidate.totalHealth / Math.max(0.001, unit.dpfOnNextHitAgainst(candidate))
    val travelPixels    = Math.max(0.0, distance - unit.pixelRangeAgainst(candidate))
    val firingPosition  = candidate.pixelCenter.project(unit.pixelCenter, unit.pixelRangeAgainst(candidate))
    val painStanding = threats
        .map(threat =>
          if (threat.inRangeToAttack(unit, firingPosition))
            MicroValue.valuePerFrameCurrentHp(threat, unit)
          else 0.0)
        .sum / threats.size
    val painWalking = PurpleMath.nanToZero(travelPixels / unit.unitClass.topSpeed) * unit.matchups.vpfReceiving
    
    val output = (painWalking + framesToKill * painStanding) / threats.size
    output
  }
  
}
