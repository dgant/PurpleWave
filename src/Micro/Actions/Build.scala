package Micro.Actions
import Lifecycle.With
import Micro.Intent.Intention

object Build extends Action {
  
  override def perform(intent: Intention): Boolean = {
  
    if (intent.toBuild.isDefined) {
      With.commander.build(intent, intent.toBuild.get, intent.destination.get)
      return true
    }
    
    false
  }
}
