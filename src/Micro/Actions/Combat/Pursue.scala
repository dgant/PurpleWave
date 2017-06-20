package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Execution.ExecutionState

object Pursue extends Action {
  
  override def allowed(state: ExecutionState): Boolean = {
    state.canFight              &&
    state.toAttack.isEmpty      &&
    state.unit.canMoveThisFrame &&
    state.targets.nonEmpty
  }
  
  override def perform(state: ExecutionState) {
    val pursuableTargets = state.targets.filter(_.topSpeed < state.unit.topSpeed * 0.9)
    state.toAttack = EvaluateTargets.best(state, pursuableTargets)
    Attack.delegate(state)
    // TODO: Chase down where the target is going to go!
  }
}
