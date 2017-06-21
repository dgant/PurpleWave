package Micro.Heuristics.Targeting

import Mathematics.Heuristics.Heuristic
import Micro.Execution.ActionState
import ProxyBwapi.UnitInfo.UnitInfo

abstract class TargetHeuristic extends Heuristic[ActionState, UnitInfo] {
  
  def evaluate(state: ActionState, candidate: UnitInfo): Double
  
}
