package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Execution.ActionState
import ProxyBwapi.Races.Protoss

object ReloadInterceptors extends Action {
  
  override def allowed(state:ActionState): Boolean =(
    state.unit.is(Protoss.Carrier)
    && With.self.minerals > Protoss.Interceptor.mineralPrice
    && state.unit.interceptors < 8
    && state.unit.trainingQueue.isEmpty
    // TODO: Stop reloading if we're about to die
  )
  
  override def perform(state:ActionState) {
    With.commander.buildInterceptor(state.unit)
  }
}
