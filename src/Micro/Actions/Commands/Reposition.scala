package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import Micro.Heuristics.Movement.EvaluatePixels
import Micro.Task.ExecutionState

object Reposition extends Action {
  
  override def allowed(state:ExecutionState) = {
    state.unit.canMoveThisFrame
  }
  
  override def perform(state:ExecutionState) {
    val pixelToMove = EvaluatePixels.best(state, state.movementProfile)
    state.movingTo = Some(pixelToMove)
    state.movedHeuristicallyFrame = With.frame
    With.commander.move(state.unit, pixelToMove)
  }
}
