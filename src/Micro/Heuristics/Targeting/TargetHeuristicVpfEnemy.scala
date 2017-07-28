package Micro.Heuristics.Targeting
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicVpfEnemy extends TargetHeuristic{
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    
    240 * candidate.matchups.vpfDealingDiffused
    
  }
}
