package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Intent.Intention

object Shoot extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    intent.canAttack &&
    intent.toAttack.isEmpty &&
    intent.unit.canAttackThisFrame &&
    intent.targetsInRange.nonEmpty
  }
  
  override def perform(intent: Intention) {
    intent.toAttack = EvaluateTargets.best(intent, intent.targetsInRange)
    Attack.delegate(intent)
  }
}
