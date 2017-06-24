package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Execution.ActionState

object Target extends Action {
  
  override protected def allowed(state: ActionState): Boolean = {
    state.canFight &&
    state.unit.canAttackThisSecond &&
    state.targets.nonEmpty
  }
  
  override protected def perform(state: ActionState) {
    state.toAttack = EvaluateTargets.best(state, state.targets)
  }
}
