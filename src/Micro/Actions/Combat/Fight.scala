package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Decisions.MicroOptions
import Micro.Execution.ActionState

object Fight extends Action {
  
  override def allowed(state: ActionState): Boolean = {
    state.unit.canMoveThisFrame || state.unit.canAttackThisFrame
  }
  
  override def perform(state: ActionState) {
    Cower.consider(state)
    Sneak.consider(state)
    if (stillReady(state)) {
      MicroOptions.choose(state.unit).execute()
    }
    /*
    BustBunker.consider(state)
    ProtectTheWeak.consider(state)
    Teamfight.consider(state)
    Pursue.consider(state)
    Pillage.consider(state)
    */
  }
}
