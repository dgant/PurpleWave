package Micro.Heuristics.Targeting
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicDamagePerSecond extends TargetHeuristic{
  
  override def evaluate(intent: Intention, candidate: UnitInfo): Double = {
    
    candidate.dpsAgainst(intent.unit) + Math.max(candidate.groundDps, candidate.airDps)
    
  }
  
}
