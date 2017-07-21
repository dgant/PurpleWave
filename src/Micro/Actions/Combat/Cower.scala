package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Execution.ActionState
import ProxyBwapi.Races.Protoss

object Cower extends Action {
  
  override protected def allowed(state: ActionState): Boolean = {
    (
      state.canCower                  ||
      state.unit.is(Protoss.Observer) || //Dirty hacks -- need to do better than this
      state.unit.is(Protoss.Arbiter)
    )                                     &&
      state.intent.unit.canMoveThisFrame  &&
      state.threats.nonEmpty              &&
      state.threats.exists(threat =>
        threat.topSpeed > state.unit.topSpeed ||
        threat.framesBeforeAttacking(state.unit) < 24.0)
  }
  
  override protected def perform(state: ActionState) {
    Disengage.delegate(state)
  }
}
