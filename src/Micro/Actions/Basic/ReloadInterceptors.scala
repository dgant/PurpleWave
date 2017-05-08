package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Intent.Intention
import ProxyBwapi.Races.Protoss

object ReloadInterceptors extends Action {
  
  override def allowed(intent: Intention): Boolean =(
    intent.unit.is(Protoss.Carrier)
    && With.self.minerals > Protoss.Interceptor.mineralPrice
    && intent.unit.interceptors < 8
    && intent.unit.trainingQueue.isEmpty
    // TODO: Stop reloading if we're about to die
  )
  
  override def perform(intent: Intention) {
    With.commander.buildInterceptor(intent)
  }
}
