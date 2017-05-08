package Micro.Actions

import Lifecycle.With
import Micro.Intent.Intention

abstract class Action {
  
  val name = getClass.getSimpleName.replaceAllLiterally("$", "")
  
  protected def stillReady(intent:Intention)  : Boolean = With.commander.ready(intent.unit)
  protected def allowed(intent:Intention)     : Boolean = true
  protected def perform(intent:Intention)
  
  def consider(intent:Intention) {
    if (stillReady(intent) && allowed(intent)) {
      intent.state.lastAction = Some(this)
      perform(intent)
    }
  }
}
