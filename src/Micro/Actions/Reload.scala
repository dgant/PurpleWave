package Micro.Actions
import Lifecycle.With
import Micro.Intent.Intention
import ProxyBwapi.Races.Protoss

object Reload extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    intent.unit.trainingQueue.isEmpty
  }
  
  override def perform(intent: Intention): Boolean = {
  
    //Repetition of scarab count is a performance optimization to avoid recalculating targets needlessly
    if (intent.unit.unitClass == Protoss.Reaver
      && intent.unit.scarabs < 5
      && intent.unit.scarabs < (if(intent.targets.isEmpty || intent.unit.cooldownLeft > 0) 5 else 1)) {
      With.commander.buildScarab(intent)
      return true
    }
  
    if (intent.unit.unitClass == Protoss.Carrier && intent.unit.interceptors < 8) {
      With.commander.buildInterceptor(intent)
      return true
    }
    
    false
  }
}
