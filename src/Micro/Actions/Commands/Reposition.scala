package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import Micro.Heuristics.Movement.EvaluatePixels
import Micro.Execution.ActionState

object Reposition extends Action {
  
  override def allowed(state:ActionState) = {
    state.unit.canMoveThisFrame
  }
  
  override def perform(state:ActionState) {
    val pixelToMove = EvaluatePixels.best(state, state.movementProfile)
    state.movingTo = Some(pixelToMove)
    state.movedHeuristicallyFrame = With.frame
    With.commander.move(state.unit, pixelToMove)
  }
}
