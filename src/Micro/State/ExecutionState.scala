package Micro.State

import Debugging.Visualization.Data.MovementHeuristicView
import Lifecycle.With
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class ExecutionState(val unit: FriendlyUnitInfo) {
  
  var intent: Intention = new Intention(With.gameplan, unit)
  var movementHeuristics: Iterable[MovementHeuristicView] = List.empty
  
}
