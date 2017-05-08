package Micro.Actions.Basic

import Micro.Actions.Action
import Micro.Actions.Combat.{Shoot, _}
import Micro.Intent.Intention

object Fight extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    intent.unit.canMoveThisFrame || intent.unit.canAttackThisFrame
  }
  
  override def perform(intent: Intention) {
    Collaborate.consider(intent)
    Shoot.consider(intent)
    Pursue.consider(intent)
  }
}
