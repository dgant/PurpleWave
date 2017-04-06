package Micro.Actions
import Lifecycle.With
import Micro.Intent.Intention
import ProxyBwapi.Races.Protoss

object Reload extends Action {
  
  override def perform(intent: Intention): Boolean = {
  
    if (intent.unit.unitClass == Protoss.Reaver
      && intent.unit.trainingQueue.isEmpty
      && intent.unit.scarabs < (if(intent.targets.isEmpty) 5 else 1)) {
      With.commander.buildScarab(intent)
      return true
    }
  
    if (intent.unit.unitClass == Protoss.Carrier
      && intent.unit.trainingQueue.isEmpty
      && intent.unit.interceptors < 8) {
      With.commander.buildInterceptor(intent)
      return true
    }
    
    false
  }
  
}
