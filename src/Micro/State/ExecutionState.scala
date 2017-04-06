package Micro.State

import Lifecycle.With
import Micro.Heuristics.MovementHeuristics.MovementHeuristicResult
import Micro.Heuristics.TargetHeuristics.TargetHeuristicResult
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class ExecutionState(val unit: FriendlyUnitInfo) {
  
  var intent: Intention = new Intention(With.gameplan, unit)
  
  var movingHeuristically       : Boolean = false
  var movementHeuristicResults  : Iterable[MovementHeuristicResult] = List.empty
  
  var targetingHeuristically    : Boolean = false
  var targetHeuristicResults    : Iterable[TargetHeuristicResult]   = List.empty
  
}
