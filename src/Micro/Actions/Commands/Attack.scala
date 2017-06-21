package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import Micro.Execution.ActionState

object Attack extends Action {
  
  override def allowed(state:ActionState): Boolean = {
    state.canFight &&
    state.toAttack.isDefined
  }
  
  override def perform(state: ActionState) {
    With.commander.attack(state.unit, state.toAttack.get)
  }
}
