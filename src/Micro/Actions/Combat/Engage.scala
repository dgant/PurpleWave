package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Intent.Intention

object Engage extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    intent.canAttack &&
    intent.canPursue &&
    intent.toAttack.isEmpty &&
    intent.unit.canMoveThisFrame &&
    intent.targets.nonEmpty &&
    {
      val zone = intent.unit.pixelCenter.zone
      ! zone.owner.isNeutral || intent.destination.exists(_.zone == zone)
    }
  }
  
  override def perform(intent: Intention) {
    Shoot.delegate(intent)
    intent.toAttack = intent.toAttack.orElse(EvaluateTargets.best(intent, intent.targets))
    Attack.delegate(intent)
  }
}
