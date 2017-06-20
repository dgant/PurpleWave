package Micro.Actions

import Lifecycle.With
import Micro.Execution.ExecutionState

abstract class Action {
  
  val name: String = getClass.getSimpleName.replaceAllLiterally("$", "")
  
  protected def allowed(state: ExecutionState): Boolean
  protected def perform(state: ExecutionState)
  
  final def consider(state: ExecutionState, giveCredit: Boolean = true) {
    if (stillReady(state) && allowed(state)) {
      if (giveCredit) state.lastAction = Some(this)
      perform(state)
    }
  }
  
  final def delegate(state: ExecutionState) {
    consider(state, giveCredit = false)
  }
  
  protected final def stillReady(state: ExecutionState): Boolean = {
    With.commander.ready(state.unit)
  }
}
