package Micro.Actions

import Lifecycle.With
import Micro.Intent.Intention

abstract class Action {
  
  val name = getClass.getSimpleName.replaceAllLiterally("$", "")
  
  protected def stillReady(intent:Intention)  : Boolean = With.commander.ready(intent.unit)
  protected def allowed(intent:Intention)     : Boolean = true
  protected def perform(intent:Intention)
  
  def consider(intent:Intention, giveCredit:Boolean = true) {
    if (stillReady(intent) && allowed(intent)) {
      if (giveCredit) { intent.state.lastAction = Some(this) }
      perform(intent)
    }
  }
  
  def delegate(intent:Intention) {
    consider(intent, giveCredit = false)
  }
}
