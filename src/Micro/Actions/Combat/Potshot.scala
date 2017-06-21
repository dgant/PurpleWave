package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Execution.ActionState

object Potshot extends Action {
  
  // If we're off cooldown, take a shot at something already in range.
  
  override def allowed(state: ActionState): Boolean = {
    state.canFight &&
    state.unit.canAttackThisFrame &&
    state.targetsInRange.nonEmpty
  }
  
  override def perform(state: ActionState) {
    state.toAttack = EvaluateTargets.best(state, state.targetsInRange)
    Attack.delegate(state)
  }
}
