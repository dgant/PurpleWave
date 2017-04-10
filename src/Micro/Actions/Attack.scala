package Micro.Actions
import Lifecycle.With
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Intent.Intention

object Attack extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    intent.canAttack && intent.unit.canAttackThisFrame
  }
  
  override def perform(intent: Intention): Boolean = {
    intent.toAttack = intent.toAttack.orElse(EvaluateTargets.best(intent, intent.targets))
    
    if (intent.toAttack.isEmpty) {
      return false
    }
    
    With.commander.attack(intent, intent.toAttack.get)
    true
  }
  
}
