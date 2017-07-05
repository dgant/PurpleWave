package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Execution.ActionState

object MineralWalk extends Action {
  
  override def allowed(state: ActionState): Boolean = {
    state.toGather.isDefined
  }
  
  override def perform(state: ActionState) {
    if (stillReady(state)) {
      With.commander.gather(state.unit, state.toGather.get)
    }
  }
}
