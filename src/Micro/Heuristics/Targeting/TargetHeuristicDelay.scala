package Micro.Heuristics.Targeting

import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicDelay extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    
    val distance      = unit.pixelsFromEdgeFast(candidate) - unit.pixelRangeAgainstFromEdge(candidate)
    val theirSpeed    = candidate.relativeSpeed(unit)
    val closingSpeed  = unit.topSpeed - theirSpeed
    val framesToClose = PurpleMath.nanToInfinity(distance / closingSpeed)
    
    24.0 + Math.max(0.0, framesToClose - unit.cooldownLeft)
  }
}
