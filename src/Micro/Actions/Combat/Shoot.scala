package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Task.ExecutionState

object Shoot extends Action {
  
  override def allowed(state:ExecutionState): Boolean = {
    state.canAttack &&
    state.toAttack.isEmpty &&
    state.unit.canAttackThisFrame &&
    state.targetsInRange.nonEmpty
  }
  
  override def perform(state:ExecutionState) {
    
    // If there are no threats, shoot anything.
    // Otherwise, don't waste cooldowns on non-combat units.
    
    val combatTargets = state.targets.filter(_.unitClass.helpsInCombat)
    
    state.toAttack = EvaluateTargets.best(
      state,
      state
        .targetsInRange
        .filter(target => combatTargets.isEmpty || target.unitClass.helpsInCombat))
    
    Attack.delegate(state)
  }
}
