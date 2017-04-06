package Micro.Heuristics.TargetHeuristics

import Mathematics.Heuristics.HeuristicMath
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicInRange extends TargetHeuristic{
  
  override def evaluate(intent: Intention, candidate: UnitInfo): Double = {
    
    HeuristicMath.fromBoolean(intent.unit.inRangeToAttack(candidate))
    
  }
  
}
