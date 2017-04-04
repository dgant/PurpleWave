package Micro.Heuristics.UnitHeuristics

import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object UnitHeuristicDistance extends UnitHeuristic{
  
  override def evaluate(intent: Intention, candidate: UnitInfo): Double = {
    
    Math.max(8.0, intent.unit.pixelsFromEdge(candidate) - intent.unit.unitClass.maxAirGroundRange)
      
  }
  
}
