package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Shoot
import Micro.State.ExecutionState

object Gather extends Action {
  
  override def allowed(state:ExecutionState) = {
    state.intent.toGather.isDefined
  }
  
  override def perform(state:ExecutionState) {
  
    Shoot.consider(state)
    
    if (stillReady(state)) {
      With.commander.gather(state.unit, state.toGather.get)
    }
  }
}
