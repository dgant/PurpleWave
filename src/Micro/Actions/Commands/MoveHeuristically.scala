package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import Micro.Heuristics.Movement.EvaluatePixels
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object MoveHeuristically extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val pixelToMove = EvaluatePixels.best(unit.agent, unit.agent.movementProfile)
    unit.agent.movingTo = Some(pixelToMove)
    unit.agent.movedHeuristicallyFrame = With.frame
    With.commander.move(unit, pixelToMove)
  }
}
