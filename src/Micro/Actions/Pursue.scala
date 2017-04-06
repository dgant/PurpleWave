package Micro.Actions
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Intent.Intention

object Pursue extends Action {
  
  override def perform(intent: Intention): Boolean = {
  
    intent.toAttack = intent.toAttack.orElse(EvaluateTargets.best(intent, intent.targetProfile, intent.targets))
    
    false
  }
  
}
