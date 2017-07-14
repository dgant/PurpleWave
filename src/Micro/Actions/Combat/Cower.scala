package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Execution.ActionState
import ProxyBwapi.Races.Protoss

object Cower extends Action {
  
  override protected def allowed(state: ActionState): Boolean = {
    state.intent.unit.canMoveThisFrame &&
      (state.canCower ||
      state.unit.is(Protoss.Observer) || //Dirty hacks -- need to do better than this
      state.unit.is(Protoss.Arbiter))
  }
  
  override protected def perform(state: ActionState) {
    Disengage.delegate(state)
  }
}
