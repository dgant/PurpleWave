package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.{Reposition, Travel}
import Micro.Behaviors.MovementProfiles
import Micro.Execution.ActionState

object Retreat extends Action {
  
  override protected def allowed(state: ActionState): Boolean = {
    state.unit.canMoveThisFrame &&
    state.unit.pixelCenter.zone != state.origin.zone &&
    state.threats.nonEmpty
  }
  
  override protected def perform(state: ActionState): Unit = {
  
    state.toTravel = Some(state.origin)
    
    if (state.unit.pixelDistanceFast(state.origin) < 128.0 || ! state.unit.melee) {
      Potshot.consider(state)
    }
    
    // Take shots while retreating if we're going to get shot anyway.
    // Good for Reavers.
    //
    if (state.unit.topSpeed < state.threats.map(_.topSpeed).min) {
      Potshot.consider(state)
    }
    
    // Are we trying to retreat from our own base?
    //
    if (state.threatsViolent.exists(threat =>
      threat.pixelCenter.zone == state.origin.zone
      &&
      state.origin.pixelDistanceFast(threat.pixelCenter) <
      state.origin.pixelDistanceFast(state.unit.pixelCenter))) {
      
      state.movementProfile = MovementProfiles.avoid
      Potshot.delegate(state)
      Reposition.delegate(state)
    }
    
    // If we have nowhere to retreat to, just fight the best we can.
    if (
      state.unit.damageInLastSecond > 0 &&
      state.threats.exists(_.inRangeToAttackFast(state.unit)) &&
      state.unit.pixelDistanceFast(state.origin) <
        state.unit.unitClass.radialHypotenuse +
        state.threats.map(_.pixelRangeAgainstFromCenter(state.unit)).max +
      16.0) {
  
      Potshot.delegate(state)
    }
    
    Travel.delegate(state)
  }
}
