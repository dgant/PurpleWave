package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Travel
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
    
    // Carriers have their own wonky retreat logic
    //
    CarrierRetreat.delegate(state)
    
    val slowerThanThreats = state.threats.forall(_.topSpeed > state.unit.topSpeed)
    lazy val trapped = state.unit.damageInLastSecond > 0 && state.threatsViolent.exists(threat =>
      threat.topSpeed * 0.8 > state.unit.topSpeed
      && threat.inRangeToAttackFast(state.unit))

    if (slowerThanThreats || trapped) {
      Potshot.consider(state)
    }
    
    // If we're a melee unit trying to defend a choke against other melee units, hold the line!
    //
    lazy val threatsAllMelee = state.threats.forall(_.melee)
    
    if (state.unit.melee && threatsAllMelee && state.unit.pixelDistanceFast(state.origin) < 16.0) {
      Potshot.consider(state)
      Travel.consider(state)
    }
    
    if (state.unit.pixelDistanceFast(state.origin) < 16.0) {
      state.movementProfile = MovementProfiles.avoid
      Potshot.delegate(state)
      Engage.delegate(state)
    }
    
    Travel.delegate(state)
  }
}
