package Micro.Actions.Basic

import Micro.Actions.Action
import Micro.Actions.Combat.{Kite, Shoot, _}
import Micro.State.ExecutionState

object Fight extends Action {
  
  override def allowed(state:ExecutionState): Boolean = {
    state.unit.canMoveThisFrame || state.unit.canAttackThisFrame
  }
  
  override def perform(state:ExecutionState) {
    Collaborate.consider(state)
    Kite.consider(state)
    Shoot.consider(state)
    Engage.consider(state)
    Retaliate.consider(state)
    Pursue.consider(state)
  }
}
