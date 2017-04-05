package Micro.Heuristics.TargetHeuristics

import Micro.Heuristics.General.MicroHeuristic
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

abstract class TargetHeuristic extends MicroHeuristic[UnitInfo] {
  
  def evaluate(intent:Intention, candidate:UnitInfo):Double
  
}
