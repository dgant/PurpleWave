package Micro.Actions
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Intent.Intention

object Poke extends Action {
  
  override def perform(intent: Intention): Boolean = {
  
    if (intent.desireToFight > 0.25) {
      intent.toAttack =
        intent.toAttack.orElse(EvaluateTargets.best(
          intent,
          intent.targetProfile,
          intent.targets.filter(target =>
            intent.unit.inRangeToAttack(target))))
    }
  
    false
  }
  
}
