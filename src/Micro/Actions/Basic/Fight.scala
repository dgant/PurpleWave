package Micro.Actions.Basic

import Micro.Actions.Action
import Micro.Actions.Combat.{Kite, Shoot, _}
import Micro.Intent.Intention

object Fight extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    intent.unit.canMoveThisFrame || intent.unit.canAttackThisFrame
  }
  
  override def perform(intent: Intention) {
    Collaborate.consider(intent)
    Kite.consider(intent)
    Shoot.consider(intent)
    Engage.consider(intent)
    Retaliate.consider(intent)
    Pursue.consider(intent)
  }
}
