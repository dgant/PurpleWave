package Micro.Heuristics.UnitHeuristics

import Micro.Intentions.Intention
import ProxyBwapi.UnitInfo.UnitInfo

trait UnitHeuristic {
  
  def evaluate(intent:Intention, candidate:UnitInfo):Double
  
}
