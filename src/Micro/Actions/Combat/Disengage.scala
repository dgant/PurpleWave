package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Execution.ActionState
import Planning.Yolo

object Disengage extends Action {
  
  override protected def allowed(state: ActionState): Boolean = {
    state.canFlee &&
    state.unit.canMoveThisFrame &&
    ! Yolo.active
  }
  
  override protected def perform(state: ActionState) {
    
    val trapped = state.threats.count(threat =>
      threat.melee
      && threat.topSpeed > state.unit.topSpeed
      && threat.pixelDistanceFast(state.unit) < 48.0) > 2
    if (trapped) {
      Potshot.delegate(state)
    }
    
    // If we're faster than all the threats we can afford to be clever.
    //
    val shouldKite = state.threats.forall(threat =>
      threat.topSpeed <= state.unit.topSpeed &&
      threat.framesBeforeAttacking(state.unit, state.unit.pixelCenter.project(threat.pixelCenter, 32.0)) > 12.0)
    
    if (shouldKite) {
      Kite.delegate(state)
    }
    else {
      Retreat.consider(state)
    }
  }
}
