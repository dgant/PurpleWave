package Micro.Actions

import Lifecycle.With
import Micro.State.ExecutionState

abstract class Action {
  
  val name = getClass.getSimpleName.replaceAllLiterally("$", "")
  
  protected def stillReady(state:ExecutionState) : Boolean = With.commander.ready(state.unit)
  protected def allowed(state:ExecutionState)    : Boolean = true
  protected def perform(state:ExecutionState)
  
  def consider(state:ExecutionState, giveCredit:Boolean = true) {
    if (stillReady(state) && allowed(state)) {
      if (giveCredit) state.lastAction = Some(this)
      perform(state)
    }
  }
  
  def delegate(state:ExecutionState) {
    consider(state, giveCredit = false)
  }
}
