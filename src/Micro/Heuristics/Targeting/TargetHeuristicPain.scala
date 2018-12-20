package Micro.Heuristics.Targeting

import Lifecycle.With
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.PurpleMath
import Micro.Decisions.MicroValue
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicPain extends TargetHeuristic {

  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    With.grids.enemyRange.get(candidate.pixelCenter.project(unit.pixelCenter, unit.pixelRangeAgainst(candidate)).tileIncluding)
  }

  def oldEvaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
  
    val threats = unit.matchups.threats.take(20)
    
    if (threats.isEmpty) return HeuristicMathMultiplicative.default
    val framesToKill    = candidate.totalHealth / Math.max(0.001, unit.dpfOnNextHitAgainst(candidate))
    val firingPosition  = unit.pixelToFireAt(candidate)
    val travelPixels    = unit.pixelDistanceCenter(firingPosition)

    val painStanding = threats
      .view
      .map(threat =>
        if (threat.inRangeToAttack(unit, firingPosition))
          MicroValue.valuePerFrameCurrentHp(threat, unit)
        else 0.0)
      .sum / unit.matchups.alliesInclSelf.size

    val painWalking = PurpleMath.nanToZero(travelPixels / unit.unitClass.topSpeed) * unit.matchups.vpfReceiving
    
    val output = (painWalking + framesToKill * painStanding) / threats.size
    output
  }
}
