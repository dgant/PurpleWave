package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Execution.ActionState

object Cower extends Action {
  
  override protected def allowed(state: ActionState): Boolean = {
    state.intent.unit.canMoveThisFrame &&
    state.canCower
  }
  
  override protected def perform(state: ActionState) {
    Disengage.delegate(state)
  }
}
