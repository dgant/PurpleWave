package Micro.Heuristics.Targeting

import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicDelay extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    
    val distanceUs    = unit.pixelsFromEdgeFast(candidate) - unit.pixelRangeAgainstFromEdge(candidate)
    val distanceTeam  = candidate.battle.map(_.teamOf(candidate).centroid.pixelDistanceFast(candidate.pixelCenter)).getOrElse(0.0)
    val distanceTotal = distanceUs + distanceTeam
    val closingSpeed  = Math.max(unit.topSpeed / 4.0, unit.topSpeed - candidate.topSpeed / 2.0)
    val framesToClose = PurpleMath.nanToInfinity(distanceTotal / closingSpeed)
    
    24.0 + Math.max(0.0, framesToClose - unit.cooldownLeft)
  }
}
