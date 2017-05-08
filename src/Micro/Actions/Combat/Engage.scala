package Micro.Actions.Combat

import Micro.Actions.Action
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
    intent.toAttack = EvaluateTargets.best(intent, intent.targets)
    //TODO: How do Zealots chase and Dragoons kite?
  }
}
