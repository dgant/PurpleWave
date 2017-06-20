package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Execution.ExecutionState

object Target extends Action {
  
  override protected def allowed(state: ExecutionState): Boolean = {
    state.canFight &&
    state.unit.canAttackThisSecond &&
    state.targets.nonEmpty
  }
  
  override protected def perform(state: ExecutionState) {
    state.toAttack = state.toAttack.orElse(EvaluateTargets.best(state, state.targets))
  }
}
