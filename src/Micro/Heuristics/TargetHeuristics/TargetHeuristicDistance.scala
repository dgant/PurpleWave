package Micro.Heuristics.TargetHeuristics

import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicDistance extends TargetHeuristic{
  
  override def evaluate(intent: Intention, candidate: UnitInfo): Double = {
    
    Math.max(
      intent.unit.rangeAgainst(candidate),
      intent.unit.pixelsFromEdgeSlow(candidate) - intent.unit.unitClass.maxAirGroundRange)
  }
}
