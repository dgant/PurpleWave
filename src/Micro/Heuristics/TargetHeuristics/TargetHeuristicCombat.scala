package Micro.Heuristics.TargetHeuristics

import Mathematics.Heuristics.HeuristicMath
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicCombat extends TargetHeuristic {
  
  override def evaluate(intent: Intention, candidate: UnitInfo): Double = {
    
    HeuristicMath.fromBoolean(candidate.unitClass.helpsInCombat)
    
  }
  
}
