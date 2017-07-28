package Micro.Heuristics.Targeting
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicPain extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
  
    val firingPixel = candidate.pixelCenter.project(
      unit.pixelCenter,
      unit.pixelRangeAgainstFromCenter(candidate))
  
    240 * unit.matchups.ifAt(firingPixel).vpfReceivingDiffused
  }
  
}
