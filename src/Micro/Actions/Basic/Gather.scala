package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Flee
import Micro.Behaviors.MovementProfiles
import Micro.State.ExecutionState

object Gather extends Action {
  
  override def allowed(state:ExecutionState) = {
    state.intent.toGather.isDefined
  }
  
  override def perform(state:ExecutionState) {
  
    // If we're threatened and continuing to gather won't help, respond
    if (
      state.threatsActive.exists(threat =>
        threat.target.contains(state.unit) &&
        threat.pixelDistanceFast(state.unit) >=
        threat.pixelDistanceFast(state.toGather.get))) {
  
      state.movementProfile  = MovementProfiles.flee
      state.toGather         = None
      state.canAttack        &&= state.threatsActive.map(_.dpsAgainst(state.unit)).sum < state.unit.totalHealth
    
      Flee.consider(state)
    }
    else {
      With.commander.gather(state.unit, state.toGather.get)
    }
  }
}
