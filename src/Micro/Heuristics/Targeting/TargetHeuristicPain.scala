package Micro.Heuristics.Targeting
import Micro.Decisions.MicroValue
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicPain extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    if (unit.matchups.threats.isEmpty) return 1.0
  
    val travelPixels  = Math.max(0.0, unit.pixelDistanceEdge(candidate) - unit.pixelRangeAgainst(candidate))
    val firingPixel   = unit.pixelCenter.project(candidate.pixelCenter, travelPixels)
    val vpfNow        = unit.matchups.vpfReceiving
    val vpfThere      = unit.matchups.threats
      .map(threat =>
        if (threat.inRangeToAttack(unit, firingPixel)) {
          val threatTargets = threat.matchups.targetsInRange.size + (if (threat.inRangeToAttack(unit)) 0.0 else 1.0)
          val threatVpfDiffused = MicroValue.valuePerFrame(threat, unit) / threatTargets
          threatVpfDiffused
        }
        else
          0.0)
      .sum
  
    240 * vpfThere - vpfNow
  }
  
}
