package Micro.Heuristics.UnitHeuristics

import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object UnitHeuristicValue extends UnitHeuristic{
  
  override def evaluate(intent: Intention, candidate: UnitInfo): Double = {
  
    candidate.unitClass.totalCost
    
  }
  
}
