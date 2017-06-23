package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Execution.ActionState

object Shove extends Action {
  
  override protected def allowed(state: ActionState): Boolean = {
    ! state.unit.flying &&
    state.unit.canMoveThisFrame &&
    state.threats.nonEmpty
  }
  
  override protected def perform(state: ActionState): Unit = {
    state.neighbors.foreach(_.actionState.shove(state.unit))
  }
}
