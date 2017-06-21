package Micro.Actions

import Lifecycle.With
import Micro.Execution.ActionState

abstract class Action {
  
  val name: String = getClass.getSimpleName.replaceAllLiterally("$", "")
  
  protected def allowed(state: ActionState): Boolean
  protected def perform(state: ActionState)
  
  final def consider(state: ActionState, giveCredit: Boolean = true) {
    if (stillReady(state) && allowed(state)) {
      if (giveCredit) state.lastAction = Some(this)
      perform(state)
    }
  }
  
  final def delegate(state: ActionState) {
    consider(state, giveCredit = false)
  }
  
  protected final def stillReady(state: ActionState): Boolean = {
    With.commander.ready(state.unit)
  }
}
