package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Shoot
import Micro.Actions.Commands.Reposition
import Micro.Task.ExecutionState

object Gather extends Action {
  
  override def allowed(state:ExecutionState) = {
    state.intent.toGather.isDefined
  }
  
  override def perform(state:ExecutionState) {
  
    Shoot.consider(state)
    
    if (state.unit.wounded && state.threatsActive.exists(_.isBeingViolentTo(state.unit))) {
      Reposition.consider(state)
    }
    
    if (stillReady(state)) {
      With.commander.gather(state.unit, state.toGather.get)
    }
  }
}
