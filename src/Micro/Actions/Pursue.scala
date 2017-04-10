package Micro.Actions
import Micro.Intent.Intention

object Pursue extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    intent.canAttack
  }
  
  override def perform(intent: Intention): Boolean = {
    
    //TODO: What if we are fleeing?
    if (true) {
      
    }
    
    false
  }
}
