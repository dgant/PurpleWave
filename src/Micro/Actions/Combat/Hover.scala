package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Reposition
import Micro.Intent.Intention

object Hover extends Action {
  
  override def allowed(intent: Intention): Boolean = (
    intent.unit.canMoveThisFrame
    && intent.targets.nonEmpty
    && intent.unit.pixelRangeMax > 32 * 3.0
  )
  
  override def perform(intent: Intention) {
    intent.canAttack = false
    val threatTargets = intent.targets.filter(_.canAttackThisSecond(intent.unit))
    if (threatTargets.nonEmpty) {
      intent.toAttack = Some(threatTargets.minBy(target => target.pixelDistanceFast(intent.unit) - target.pixelRangeAgainst(intent.unit)))
      Reposition.delegate(intent)
    }
    
  }
}
