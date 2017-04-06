package Micro.Actions
import Lifecycle.With
import Micro.Intent.Intention

object Flee extends Action {
  
  override def perform(intent: Intention): Boolean = {
  
    if (intent.desireToFight < 1.0 && intent.threats.nonEmpty) {
      intent.destination = Some(With.geography.home)
    }
    
    false
  }
  
}
