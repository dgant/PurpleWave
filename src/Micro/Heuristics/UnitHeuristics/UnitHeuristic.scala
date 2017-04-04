package Micro.Heuristics.UnitHeuristics

import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

trait UnitHeuristic {
  
  def evaluate(intent:Intention, candidate:UnitInfo):Double
  
}
