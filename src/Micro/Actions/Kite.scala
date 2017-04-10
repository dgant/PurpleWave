package Micro.Actions
import Information.Battles.Simulation.Tactics.TacticMovement
import Micro.Behaviors.MovementProfiles
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Intent.Intention

object Kite extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    intent.unit.canMove && intent.tactics.exists(_.movement == TacticMovement.Kite)
  }
  
  override def perform(intent: Intention): Boolean = {
    intent.movementProfile.combine(MovementProfiles.kite)

    if (intent.unit.canAttackThisFrame) {
      intent.toAttack = intent.toAttack.orElse(
        EvaluateTargets.best(
          intent,
          intent.targets.filter(intent.unit.inRangeToAttack)))
    }
  
    false
  }
}
