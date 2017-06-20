package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import Micro.Execution.ExecutionState

object Attack extends Action {
  
  override def allowed(state:ExecutionState): Boolean = {
    state.canFight &&
    state.toAttack.isDefined
  }
  
  override def perform(state: ExecutionState) {
    With.commander.attack(state.unit, state.toAttack.get)
  }
}
