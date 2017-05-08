package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Intent.Intention
import ProxyBwapi.Races.Protoss

object ReloadScarabs extends Action {
  
  override def allowed(intent: Intention): Boolean = (
    
    //Repetition of scarab count check is a performance optimization to avoid calculating targets needlessly
    
    intent.unit.is(Protoss.Reaver)
    && With.self.minerals > Protoss.Scarab.mineralPrice
    && intent.unit.scarabs < 5
    && intent.unit.scarabs < (if(intent.targetsInRange.isEmpty || intent.unit.cooldownLeft > 0) 5 else 1)
    && intent.unit.trainingQueue.isEmpty
    
    // TODO: Stop reloading if we're about to die
  )
  
  override def perform(intent: Intention) {
    With.commander.buildScarab(intent)
  }
}
