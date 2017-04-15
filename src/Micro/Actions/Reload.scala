package Micro.Actions
import Lifecycle.With
import Micro.Intent.Intention
import ProxyBwapi.Races.Protoss

object Reload extends Action {
  
  override def allowed(intent: Intention): Boolean = true
  
  override def perform(intent: Intention): Boolean = {
  
    //Repetition of scarab count is a performance optimization to avoid recalculating targets needlessly
    if (intent.unit.is(Protoss.Reaver)
      && With.self.minerals > Protoss.Scarab.mineralPrice
      && intent.unit.scarabs < 5
      && intent.unit.scarabs < (if(intent.targets.isEmpty || intent.unit.cooldownLeft > 0) 5 else 1)
      && intent.unit.trainingQueue.isEmpty) {
      With.commander.buildScarab(intent)
      return true
    }
  
    if (intent.unit.is(Protoss.Carrier)
      && With.self.minerals > Protoss.Interceptor.mineralPrice
      && intent.unit.interceptors < 8
      && intent.unit.trainingQueue.isEmpty) {
      With.commander.buildInterceptor(intent)
      return true
    }
    
    false
  }
}
