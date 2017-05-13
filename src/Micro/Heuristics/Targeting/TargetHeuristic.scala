package Micro.Heuristics.Targeting

import Mathematics.Heuristics.Heuristic
import Micro.Task.ExecutionState
import ProxyBwapi.UnitInfo.UnitInfo

abstract class TargetHeuristic extends Heuristic[ExecutionState, UnitInfo] {
  
  def evaluate(state: ExecutionState, candidate: UnitInfo):Double
  
}
