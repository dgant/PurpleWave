package Micro.Heuristics.TargetHeuristics
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicDamagePerSecond extends TargetHeuristic{
  
  override def evaluate(intent: Intention, candidate: UnitInfo): Double = {
    
    Math.max(candidate.groundDps, candidate.airDps)
    
  }
  
}
