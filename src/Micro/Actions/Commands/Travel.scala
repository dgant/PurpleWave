package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import Micro.State.ExecutionState

object Travel extends Action {
  
  override def allowed(state:ExecutionState) = {
    state.unit.canMoveThisFrame &&
    state.toTravel.isDefined
  }
  
  override def perform(state:ExecutionState) {
    val pixelToMove = state.toTravel.get
    state.movingTo = Some(pixelToMove)
    With.commander.move(state.unit, pixelToMove)
  }
}
