package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Execution.ActionState

object Unstick extends Action {
  
  override protected def allowed(state: ActionState): Boolean = {
    state.unit.canMoveThisFrame &&
    state.unit.failingToMove
  }
  
  override protected def perform(state: ActionState): Unit = {
    With.commander.stop(state.unit)
  }
}
