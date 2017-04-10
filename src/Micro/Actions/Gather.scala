package Micro.Actions
import Lifecycle.With
import Micro.Intent.Intention

object Gather extends Action {
  
  override def allowed(intent: Intention) = {
    intent.toGather.isDefined
  }
  
  override def perform(intent: Intention): Boolean = {
    With.commander.gather(intent, intent.toGather.get)
    true
  }
}
