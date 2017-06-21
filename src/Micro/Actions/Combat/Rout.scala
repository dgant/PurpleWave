package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.{Reposition, Travel}
import Micro.Behaviors.MovementProfiles
import Micro.Execution.ExecutionState

object Rout extends Action {
  
  override protected def allowed(state: ExecutionState): Boolean = {
    state.unit.canMoveThisFrame &&
    state.unit.pixelCenter.zone != state.origin.zone
  }
  
  override protected def perform(state: ExecutionState): Unit = {
    
    if (state.unit.pixelDistanceFast(state.origin) < 128.0) {
      Potshot.consider(state)
    }
    
    // Are we blocked from running away?
    //
    if (state.threatsActive.exists(threat =>
      threat.pixelCenter.zone == state.origin.zone &&
      state.origin.pixelDistanceFast(threat.pixelCenter) <
      state.origin.pixelDistanceFast(state.unit.pixelCenter))) {
      
      state.movementProfile = MovementProfiles.rout
      Reposition.delegate(state)
    }
    else {
      state.toTravel = Some(state.origin)
      Travel.delegate(state)
    }
  }
  
}
