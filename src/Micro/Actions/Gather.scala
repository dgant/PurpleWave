package Micro.Actions
import Lifecycle.With
import Micro.Intent.Intention

object Gather extends Action {
  
  override def perform(intent: Intention): Boolean = {
    
    if (intent.toGather.isDefined) {
      With.commander.gather(intent, intent.toGather.get)
      return true
    }
    
    false
  }
}
