package Micro.State

import Lifecycle.With
import Micro.Heuristics.MovementHeuristics.MovementHeuristicResult
import Micro.Heuristics.TargetHeuristics.TargetHeuristicResult
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class ExecutionState(val unit: FriendlyUnitInfo) {
  
  var intent: Intention = new Intention(With.gameplan, unit)
  
  var movementHeuristicResults  : Iterable[MovementHeuristicResult] = List.empty
  var targetHeuristicResults    : Iterable[TargetHeuristicResult]   = List.empty
  
}
