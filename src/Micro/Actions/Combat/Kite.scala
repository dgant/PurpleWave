package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.State.ExecutionState

object Kite extends Action {
  
  override def allowed(state:ExecutionState): Boolean = (
    state.unit.canMoveThisFrame
    && state.targets.nonEmpty
    && state.threats.nonEmpty
    && state.unit.pixelRangeMax > 32 * 3.0
  )
  
  override def perform(state:ExecutionState) {
    
    if (state.unit.cooldownLeft > 0) {
      Hover.delegate(state)
      return
    }
    
    // If we can kite perfectly, do so
    // If we can't, just try to maximize the damage we deal compared to damage received
    
    val weAreFaster   = state.threatsActive.forall(_.topSpeed <= state.unit.topSpeed)
    val enemyIsFaster = state.threatsActive.exists(_.topSpeed > state.unit.topSpeed)
  
    val framesToShootAndMove = 8
    // If we can kite perfectly without taking damage (vultures/dragoons vs. slow zealots, for instance), let's try to do so.
    // This might backfire in closed spaces, by causing us to not fire when we otherwise could
    //
    if (weAreFaster
      && state.threatsActive.exists(threat =>
        threat.framesBeforeAttacking(state.unit) < framesToShootAndMove ||
        threat.pixelDistanceTravelling(state.unit.pixelCenter) <
          (if (state.unit.canAttackThisSecond(threat))
            state.unit.pixelRangeAgainst(threat)
          else
            state.unit.pixelRangeMax - 80.0))) {
      
      Hover.delegate(state)
    }
    
    Engage.delegate(state)
  }
}
