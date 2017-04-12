package Micro.State

import Lifecycle.With
import Micro.Heuristics.MovementHeuristics.MovementHeuristicResult
import Micro.Heuristics.TargetHeuristics.TargetHeuristicResult
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.TilePosition

class ExecutionState(val unit: FriendlyUnitInfo) {
  
  var intent: Intention = new Intention(With.gameplan, unit)

  var movement                  : Option[TilePosition] = None
  var movingHeuristically       : Boolean = false
  var movementHeuristicResults  : Iterable[MovementHeuristicResult] = Vector.empty
  
  var target                    : Option[UnitInfo] = None
  var targetingHeuristically    : Boolean = false
  var targetHeuristicResults    : Iterable[TargetHeuristicResult]   = Vector.empty
}
