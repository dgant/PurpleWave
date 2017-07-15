package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Travel
import Micro.Execution.ActionState
import ProxyBwapi.Races.Protoss

object CarrierRetreat extends Action {
  
  override protected def allowed(state: ActionState): Boolean = {
    state.unit.is(Protoss.Carrier)
  }
  
  // Carriers can't afford to be fancy in their retreating.
  override protected def perform(state: ActionState): Unit = {
    state.toTravel = Some(state.origin)
    Travel.delegate(state)
  }
}
