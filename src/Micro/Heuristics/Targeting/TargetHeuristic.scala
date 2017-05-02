package Micro.Heuristics.Targeting

import Mathematics.Heuristics.Heuristic
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

abstract class TargetHeuristic extends Heuristic[Intention, UnitInfo] {
  
  def evaluate(intent:Intention, candidate:UnitInfo):Double
  
}
