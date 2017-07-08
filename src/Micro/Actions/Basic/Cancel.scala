package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Execution.ActionState

object Cancel extends Action {
  
  override def allowed(state: ActionState): Boolean = {
    ! state.unit.complete &&
    state.unit.unitClass.isBuilding &&
    state.unit.totalHealth < state.unit.damageInLastSecond * 2
  }
  
  override def perform(state: ActionState) {
    With.commander.cancel(state.unit)
  }
}
