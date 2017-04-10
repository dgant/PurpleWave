package Micro.Actions
import Lifecycle.With
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Intent.Intention

object Attack extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    intent.canAttack && intent.toAttack.isDefined && intent.unit.canAttackThisFrame
  }
  
  override def perform(intent: Intention): Boolean = {
    intent.toAttack = intent.toAttack.orElse(EvaluateTargets.best(intent, intent.targets))
    With.commander.attack(intent, intent.toAttack.get)
    true
  }
  
}
