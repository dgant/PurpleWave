package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Execution.ActionState

object Pursue extends Action {
  
  override def allowed(state: ActionState): Boolean = {
    state.canFight              &&
    state.toAttack.isEmpty      &&
    state.unit.canMoveThisFrame &&
    state.targets.nonEmpty
  }
  
  override def perform(state: ActionState) {
    val pursuableTargets = state.targets.filter(_.topSpeed < state.unit.topSpeed * 0.9)
    
    state.toAttack = EvaluateTargets.best(state, pursuableTargets)
    if (state.unit.canAttackThisFrame) {
      Attack.delegate(state)
    }
    else {
      state.toAttack.foreach(target => state.toTravel = Some(target.targetPixel.getOrElse(target.project(state.unit.cooldownLeft))))
    }
  }
}
