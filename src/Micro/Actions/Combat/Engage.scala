package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Execution.ActionState
import Micro.Heuristics.Targeting.EvaluateTargets

object Engage extends Action {
  
  override def allowed(state: ActionState): Boolean = {
    state.canFight &&
    state.targets.nonEmpty
  }
  
  override def perform(state: ActionState) {
    Brawl.consider(state)
    BustWallin.consider(state)
    chooseTarget(state)
    if ( ! state.unit.canAttackThisFrame) {
      Kite.delegate(state)
    }
    Attack.delegate(state)
  }
  
  def chooseTarget(state: ActionState) {
    if (state.toAttack.isDefined) {
      return
    }
    val targets = state.targets.filter(target =>
      state.unit.inRangeToAttackFast(target)
      || target.isBeingViolent
      || target.gathering
      || target.repairing
      || target.topSpeed < state.unit.topSpeed * 0.75)
    state.toAttack = EvaluateTargets.best(state, targets)
  }
}
