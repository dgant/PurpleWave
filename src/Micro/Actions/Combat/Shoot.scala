package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Intent.Intention

object Shoot extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    intent.unit.canAttackThisFrame &&
    intent.toAttack.isEmpty
  }
  
  override def perform(intent: Intention) {
    intent.toAttack = EvaluateTargets.best(intent, intent.targetsInRange)
    Attack.consider(intent)
  }
}
