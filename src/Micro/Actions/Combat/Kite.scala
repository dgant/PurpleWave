package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Execution.ExecutionState

object Kite extends Action {
  
  override def allowed(state: ExecutionState): Boolean = (
    state.unit.canMoveThisFrame
    && state.targets.nonEmpty
    && state.threats.nonEmpty
    && state.unit.pixelRangeMax > 32 * 3.0
  )
  
  override def perform(state: ExecutionState) {
  
    // TODO: Better concept than outspeeding is ASYMPTOTIC FRAMES OF SAFETY.
    // * They are faster: 0
    // * We are faster: Infinite
    // * We match their top speed: Frames before hitting us
    // so if we're fighting both Zealots and Dragoons, we outspeed if we're out of Dragoon range for long enough to shoot
    
    val canAttackThisFrame = state.unit.canAttackThisFrame
    if (canAttackThisFrame) {
      
      val activelyThreatened = state.threatsActive.nonEmpty
      
      if (activelyThreatened) {
        
        // Before shooting, make sure we have ample space
        // Also, don't close distance unless we are faster
        throw new NotImplementedError()
        
        val sufficientSpace = true // TODO: Consider both Dragoons vs. Zealots AND Dragoons vs. Zealots + Dragoons
        if (sufficientSpace) {
          Potshot.consider(state)
        } else {
          // TODO: Back off
        }
      } else {
        // If we're not in danger, fire.
        Potshot.consider(state)
      }
    }
    
    // Back off. If we outspeed, sit at the sweet spot of range. If we don't, just get as far as possible.
    
    val weOutspeed = true // TODO
    if (weOutspeed) {
      //TODO: Sit at sweet spot
      throw new NotImplementedError()
    }
    else {
      //TODO: Rout? Or are there situations where we just need to DEAL WITH IT AND FIGHT?
      //Maybe we should just do nothing and let the delegator figure it out.
      throw new NotImplementedError()
    }
  
  
    ////////////////////////////
    // OLD LOGIC -- to REMOVE ///////////////////////////////////////////////////
    ////////////////////////////
    
    // If we're on cooldown, back off.
    // If we're off cooldown:
    //  If they're pursuing us, and we're faster, get AMPLE distance before firing back
    //  If we're not being pursued they're not pursuing us, fire
    
    if (state.unit.cooldownLeft > 0) {
      HoverOutsideRange.delegate(state)
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
            state.unit.pixelRangeAgainstFromEdge(threat)
          else
            state.unit.pixelRangeMax - 80.0))) {
      
      HoverOutsideRange.delegate(state)
    }
    
    Engage.delegate(state)
  }
}
