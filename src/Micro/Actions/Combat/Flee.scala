package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.{Reposition, Travel}
import Micro.State.ExecutionState

object Flee extends Action {
  
  override def allowed(state:ExecutionState) = {
    state.unit.canMoveThisFrame &&
    state.threats.nonEmpty
  }
  
  override def perform(state:ExecutionState) {
    
    state.canPursue = false
    state.toTravel  = Some(state.origin)
  
    val enemyFaster = state.threatsActive.exists(threat => threat.topSpeed > state.unit.topSpeed)
    val weAreFaster = state.threatsActive.forall(threat => threat.topSpeed < state.unit.topSpeed)
  
    // If the enemy is faster, go straight home so we don't get caught
    if (enemyFaster) {
      Travel.delegate(state)
    }
  
    //If we're faster, we can be cuter with how we retreat
    if (weAreFaster) {
      Reposition.delegate(state)
    }
  
    // If we have a clear path home, then skip heuristic movement and just go.
    val ourDistanceToOrigin = state.unit.pixelDistanceTravelling(state.origin) - 32.0
    if (state.threatsActive.forall(threat =>
      ourDistanceToOrigin <= (
        if (state.unit.flying) threat.pixelDistanceFast(state.origin) //Don't retreat directly over the enmy!
        else                    threat.pixelDistanceTravelling(state.origin)))) {
    
      Travel.delegate(state)
    }
  
    Reposition.delegate(state)
  }
}
