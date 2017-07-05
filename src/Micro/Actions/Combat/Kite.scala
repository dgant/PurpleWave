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
    if (state.unit.canAttackThisFrame) {
      val fasterThanActiveThreats = state.threatsViolent.nonEmpty || state.threatsViolent.forall(state.unit.topSpeed > _.topSpeed)
      if (fasterThanActiveThreats) {
      
        // Before shooting, make sure we have ample space
        // Also, don't close distance unless we are faster
      
        val sufficientSpace = state.threatsViolent.forall(_.framesBeforeAttacking(state.unit) > state.unit.framesPerAttack)
        if (sufficientSpace) {
          Potshot.consider(state)
        } else {
          HoverOutsideRange.delegate(state)
        }
      } else {
        // If we're not in danger (or can't escape anyway) fire.
        Potshot.consider(state)
      }
    }
    else {
      
      // Back off. If we outspeed, sit at the sweet spot of range. If we don't, just get as far as possible.
      val weOutspeedActiveThreats = state.threatsViolent.forall(state.unit.topSpeed > _.topSpeed)
      if (weOutspeedActiveThreats) {
        HoverOutsideRange.delegate(state)
      }
      else {
        Retreat.delegate(state)
      }
    }
  }
}
