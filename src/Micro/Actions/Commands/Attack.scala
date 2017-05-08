package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import Micro.Intent.Intention

object Attack extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    intent.canAttack &&
    intent.toAttack.isDefined
  }
  
  override def perform(intent: Intention) {
    With.commander.attack(intent.unit, intent.toAttack.get)
  }
}
