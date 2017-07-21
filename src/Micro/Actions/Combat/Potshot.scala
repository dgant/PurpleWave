package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Execution.ActionState

object Potshot extends Action {
  
  // If we're off cooldown, take a shot at something already in range.
  
  override def allowed(state: ActionState): Boolean = {
    state.canFight &&
    state.unit.readyForAttackOrder &&
    state.targetsInRange.nonEmpty
  }
  
  override def perform(state: ActionState) {
    val validTargets = state.targetsInRange.filter(_.unitClass.helpsInCombat)
    state.toAttack = EvaluateTargets.best(state, validTargets)
    Attack.delegate(state)
  }
}
