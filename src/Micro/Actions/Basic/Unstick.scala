package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Execution.ActionState
import ProxyBwapi.Races.Protoss

object Unstick extends Action {
  
  override protected def allowed(state: ActionState): Boolean = {
    state.unit.canMoveThisFrame &&
    state.unit.seeminglyStuck   &&
    ! state.unit.is(Protoss.Carrier)
  }
  
  override protected def perform(state: ActionState): Unit = {
    With.commander.stop(state.unit)
  }
}
