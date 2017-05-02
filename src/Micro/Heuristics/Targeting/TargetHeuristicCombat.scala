package Micro.Heuristics.Targeting

import Mathematics.Heuristics.HeuristicMathMultiplicative
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicCombat extends TargetHeuristic {
  
  override def evaluate(intent: Intention, candidate: UnitInfo): Double = {
    
    HeuristicMathMultiplicative.fromBoolean(candidate.unitClass.helpsInCombat)
    
  }
  
}
