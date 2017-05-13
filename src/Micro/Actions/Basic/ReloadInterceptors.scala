package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Task.ExecutionState
import ProxyBwapi.Races.Protoss

object ReloadInterceptors extends Action {
  
  override def allowed(state:ExecutionState): Boolean =(
    state.unit.is(Protoss.Carrier)
    && With.self.minerals > Protoss.Interceptor.mineralPrice
    && state.unit.interceptors < 8
    && state.unit.trainingQueue.isEmpty
    // TODO: Stop reloading if we're about to die
  )
  
  override def perform(state:ExecutionState) {
    With.commander.buildInterceptor(state.unit)
  }
}
