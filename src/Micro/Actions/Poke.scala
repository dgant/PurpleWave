package Micro.Actions
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Intent.Intention

object Poke extends Action {
  
  override def perform(intent: Intention): Boolean = {
  
    intent.toAttack =
      intent.toAttack.orElse(EvaluateTargets.best(
        intent,
        intent.targetProfile,
        intent.targets.filter(intent.unit.inRangeToAttack)))
  
    false
  }
  
}
