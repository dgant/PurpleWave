package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.{Reposition, Travel}
import Micro.Behaviors.MovementProfiles
import Micro.Execution.ActionState

object Retreat extends Action {
  
  override protected def allowed(state: ActionState): Boolean = {
    state.unit.canMoveThisFrame &&
    state.unit.pixelCenter.zone != state.origin.zone
  }
  
  override protected def perform(state: ActionState): Unit = {
    
    if (state.unit.pixelDistanceFast(state.origin) < 128.0) {
      Potshot.consider(state)
    }
    
    // Are we blocked from running away?
    //
    if (state.threatsViolent.exists(threat =>
      threat.pixelCenter.zone == state.origin.zone &&
      state.origin.pixelDistanceFast(threat.pixelCenter) <
      state.origin.pixelDistanceFast(state.unit.pixelCenter))) {
      
      state.movementProfile = MovementProfiles.retreat
      Reposition.delegate(state)
    }
    // If we have nowhere to retreat to, just fight the best we can.
    else if (
      state.threats.nonEmpty &&
        state.unit.pixelDistanceFast(state.origin) <
        state.unit.unitClass.radialHypotenuse +
        state.threats.map(_.pixelRangeAgainstFromCenter(state.unit)).max +
        16.0) {
  
      state.toTravel = Some(state.origin)
      Engage.delegate(state)
    }
    else {
      state.toTravel = Some(state.origin)
      Travel.delegate(state)
    }
  }
}
