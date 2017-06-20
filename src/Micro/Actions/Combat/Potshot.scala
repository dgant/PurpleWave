package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Execution.ExecutionState

object Potshot extends Action {
  
  // If we're off cooldown, take a shot at something already in range.
  
  override def allowed(state: ExecutionState): Boolean = {
    state.canFight &&
    state.unit.canAttackThisFrame
  }
  
  override def perform(state: ExecutionState) {
    state.toAttack = EvaluateTargets.best(state, state.targetsInRange)
    Attack.delegate(state)
  }
}
