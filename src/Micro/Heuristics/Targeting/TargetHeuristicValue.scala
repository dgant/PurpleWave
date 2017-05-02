package Micro.Heuristics.Targeting

import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicValue extends TargetHeuristic{
  
  override def evaluate(intent: Intention, candidate: UnitInfo): Double = {
  
    candidate.unitClass.totalCost
    
  }
  
}
