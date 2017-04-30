package Micro.Actions
import Lifecycle.With
import Micro.Intent.Intention

object Attack extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    intent.canAttack && intent.toAttack.isDefined && intent.unit.canAttackThisFrame
  }
  
  override def perform(intent: Intention): Boolean = {
    With.commander.attack(intent, intent.toAttack.get)
    return true
  }
}
