package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Potshot
import Micro.Execution.ActionState

object Gather extends Action {
  
  override def allowed(state: ActionState): Boolean = {
    state.toGather.isDefined
  }
  
  override def perform(state: ActionState) {
  
    Potshot.consider(state)
    
    // TODO: Run away!
    
    if (stillReady(state)) {
      With.commander.gather(state.unit, state.toGather.get)
    }
  }
}
