package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Execution.ActionState

object Kite extends Action {
  
  override def allowed(state: ActionState): Boolean = (
    state.unit.canMoveThisFrame
    && state.targets.nonEmpty
    && state.threats.nonEmpty
    && state.unit.pixelRangeMax > 32 * 3.0
  )
  
  override def perform(state: ActionState) {
  
    lazy val fasterThanThreats  = state.threats.forall(threat => state.unit.topSpeed > threat.topSpeed)
    lazy val slowerThanThreats  = state.threats.forall(threat => state.unit.topSpeed < threat.topSpeed)
    
    if (state.unit.readyForAttackOrder) {
      if (fasterThanThreats) {
      
        // Before shooting, make sure we have ample space
        // Also, don't close distance unless we are faster
      
        val sufficientSpace = state.threatsViolent.forall(_.framesBeforeAttacking(state.unit) > state.unit.unitClass.stopFrames + state.unit.unitClass.minStop)
        if (sufficientSpace) {
          Potshot.consider(state)
        } else {
          HoverOutsideRange.delegate(state)
        }
      } else if (slowerThanThreats) {
        // We can't outrun them so might as well shoot
        // (This is bad vs. other units of same type)
        Potshot.consider(state)
      }
    }
    
    if (fasterThanThreats) {
      HoverOutsideRange.delegate(state)
    }
    else {
      Retreat.delegate(state)
    }
  }
}
