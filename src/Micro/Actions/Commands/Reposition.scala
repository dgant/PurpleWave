package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import Micro.Heuristics.Movement.EvaluatePixels
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Reposition extends Action {
  
  override def allowed(unit: FriendlyUnitInfo) = {
    unit.canMoveThisFrame
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val pixelToMove = EvaluatePixels.best(unit.action, unit.action.movementProfile)
    unit.action.movingTo = Some(pixelToMove)
    unit.action.movedHeuristicallyFrame = With.frame
    With.commander.move(unit, pixelToMove)
  }
}
