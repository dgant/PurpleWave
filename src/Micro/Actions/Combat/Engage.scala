package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.State.ExecutionState

object Engage extends Action {
  
  override def allowed(state:ExecutionState): Boolean = {
    state.canAttack &&
    state.canPursue &&
    state.toAttack.isEmpty &&
    state.unit.canMoveThisFrame &&
    state.targets.nonEmpty &&
    {
      val zone = state.unit.pixelCenter.zone
      ! zone.owner.isNeutral || state.toTravel.exists(_.zone == zone)
    }
  }
  
  override def perform(state:ExecutionState) {
    state.toAttack = state.toAttack.orElse(EvaluateTargets.best(state, state.targets))
    Attack.delegate(state)
  }
}
