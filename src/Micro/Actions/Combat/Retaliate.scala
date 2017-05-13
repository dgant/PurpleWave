package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Task.ExecutionState

object Retaliate extends Action {
  
  override def allowed(state:ExecutionState): Boolean = {
    state.canAttack &&
    state.canPursue &&
    state.toAttack.isEmpty &&
    state.unit.canMoveThisFrame &&
    state.targets.nonEmpty &&
    state.threatsActive.nonEmpty
  }
  
  override def perform(state:ExecutionState) {
    state.toAttack = EvaluateTargets.best(state, state.targets.intersect(state.threatsActive))
    Attack.delegate(state)
  }
}
