package Micro.Actions.Combat

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Commands.Reposition
import Micro.Task.ExecutionState

object Cower extends Action {
  
  override protected def allowed(state: ExecutionState): Boolean = {
    state.intent.unit.canMoveThisFrame &&
    state.intent.canCower
  }
  
  override protected def perform(state: ExecutionState) {
    
    if (
      With.grids.dpsEnemy.get(state.unit.tileIncludingCenter,                                 state.unit) > 0 ||
      With.grids.dpsEnemy.get(state.unit.project(With.latency.framesRemaining).tileIncluding, state.unit) > 0) {
      state.movementProfile.avoidDamage = 5.0
      Reposition.delegate(state)
    }
  }
}
