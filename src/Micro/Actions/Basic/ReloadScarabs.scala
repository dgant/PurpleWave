package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Execution.ActionState
import ProxyBwapi.Races.Protoss

object ReloadScarabs extends Action {
  
  override def allowed(state:ActionState): Boolean = (
    
    //Repetition of scarab count check is a performance optimization to avoid calculating targets needlessly
  
    state.unit.is(Protoss.Reaver)
    && With.self.minerals > Protoss.Scarab.mineralPrice
    && state.unit.scarabs < 5
    && state.unit.scarabs < (if(state.targetsInRange.isEmpty || state.unit.cooldownLeft > 0) 5 else 1)
    && state.unit.trainingQueue.isEmpty
    
    // TODO: Stop reloading if we're about to die
  )
  
  override def perform(state:ActionState) {
    With.commander.buildScarab(state.unit)
  }
}
