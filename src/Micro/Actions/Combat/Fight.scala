package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Execution.ActionState

object Fight extends Action {
  
  override def allowed(state: ActionState): Boolean = {
    state.unit.canMoveThisFrame || state.unit.canAttackThisFrame
  }
  
  override def perform(state: ActionState) {
    Cower.consider(state)
    Sneak.consider(state)
    BustBunker.consider(state)
    ProtectTheWeak.consider(state)
    Teamfight.consider(state)
    Pursue.consider(state)
    Pillage.consider(state)
  }
}
