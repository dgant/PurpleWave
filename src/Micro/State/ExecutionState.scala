package Micro.State

import Lifecycle.With
import Mathematics.Pixels.Pixel
import Micro.Actions.Action
import Micro.Heuristics.MovementHeuristics.MovementHeuristicResult
import Micro.Heuristics.TargetHeuristics.TargetHeuristicResult
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class ExecutionState(val unit: FriendlyUnitInfo) {
  
  var intent: Intention = new Intention(With.gameplan, unit)

  var lastAction: Option[Action] = None
  
  var movingTo                  : Option[Pixel] = None
  var movedHeuristicallyFrame   : Int = 0
  var movementHeuristicResults  : Iterable[MovementHeuristicResult] = Vector.empty
  
  var target                      : Option[UnitInfo] = None
  var targetedHeuristicallyFrame  : Int = 0
  var targetHeuristicResults      : Iterable[TargetHeuristicResult]   = Vector.empty
}
