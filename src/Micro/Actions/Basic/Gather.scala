package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.{Flee, Potshot}
import Micro.Execution.ExecutionState

object Gather extends Action {
  
  override def allowed(state: ExecutionState): Boolean = {
    state.intent.toGather.isDefined
  }
  
  override def perform(state: ExecutionState) {
  
    Potshot.consider(state)
    
    if (state.threatsActive.exists(_.isBeingViolentTo(state.unit))) {
      Flee.consider(state)
    }
    
    if (stillReady(state)) {
      With.commander.gather(state.unit, state.toGather.get)
    }
  }
}
