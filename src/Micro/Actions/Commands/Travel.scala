package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import Micro.Execution.ActionState

object Travel extends Action {
  
  override def allowed(state:ActionState) = {
    state.unit.canMoveThisFrame &&
    state.toTravel.isDefined
  }
  
  override def perform(state:ActionState) {
    val pixelToMove = state.toTravel.get
    state.movingTo = Some(pixelToMove)
    With.commander.move(state.unit, pixelToMove)
  }
}
