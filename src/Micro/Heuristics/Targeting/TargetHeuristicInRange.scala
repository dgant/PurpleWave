package Micro.Heuristics.Targeting

import Mathematics.Heuristics.HeuristicMathMultiplicative
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicInRange extends TargetHeuristic{
  
  override def evaluate(intent: Intention, candidate: UnitInfo): Double = {
    
    HeuristicMathMultiplicative.fromBoolean(intent.unit.inRangeToAttackFast(candidate))
    
  }
  
}
