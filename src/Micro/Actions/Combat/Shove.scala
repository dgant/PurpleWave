package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Execution.ActionState

object Shove extends Action {
  
  override protected def allowed(state: ActionState): Boolean = {
    ! state.unit.flying &&
    state.unit.canMoveThisFrame &&
    state.unit.matchups.threats.nonEmpty
  }
  
  override protected def perform(state: ActionState): Unit = {
    state.unit.matchups.allies.foreach(_.actionState.shove(state.unit))
  }
}
