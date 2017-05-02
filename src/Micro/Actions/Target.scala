package Micro.Actions

import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Intent.Intention

object Target extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    intent.canAttack
  }
  
  override def perform(intent: Intention): Boolean = {
    val targets = if (intent.canPursue) intent.targets else intent.targetsInRange
    intent.toAttack = intent.toAttack.orElse(EvaluateTargets.best(intent, targets))
    false
  }
}
