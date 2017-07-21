package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ReloadScarabs extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    
    //Repetition of scarab count check is a performance optimization to avoid calculating targets needlessly
  
    unit.is(Protoss.Reaver)
    && With.self.minerals > Protoss.Scarab.mineralPrice
    && unit.scarabCount < With.configuration.maxScarabCount
    && unit.scarabCount < (if(unit.matchups.targetsInRange.isEmpty || unit.cooldownLeft > 0) With.configuration.maxScarabCount else 1)
    && unit.trainingQueue.isEmpty
  )
  
  override def perform(unit: FriendlyUnitInfo) {
    With.commander.buildScarab(unit)
  }
}
