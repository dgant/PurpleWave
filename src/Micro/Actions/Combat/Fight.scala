package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Execution.ExecutionState

object Fight extends Action {
  
  override def allowed(state: ExecutionState): Boolean = {
    state.unit.canMoveThisFrame || state.unit.canAttackThisFrame
  }
  
  override def perform(state: ExecutionState) {
    BustBunker.consider(state)
    ProtectTheWeak.consider(state)
    Teamfight.consider(state)
    Pursue.consider(state)
  }
}
