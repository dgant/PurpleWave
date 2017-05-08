package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Intent.Intention

object Retaliate extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    intent.canAttack &&
    intent.canPursue &&
    intent.toAttack.isEmpty &&
    intent.unit.canMoveThisFrame &&
    intent.targets.nonEmpty &&
    intent.threatsActive.nonEmpty
  }
  
  override def perform(intent: Intention) {
    intent.toAttack = EvaluateTargets.best(intent, intent.targets.intersect(intent.threatsActive))
    Attack.delegate(intent)
  }
}
