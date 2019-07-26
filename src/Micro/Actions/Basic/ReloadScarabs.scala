package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ReloadScarabs extends Action {
  
  val maxScarabCount = 3
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    
    //Repetition of scarab count check is a performance optimization to avoid calculating targets needlessly
    unit.is(Protoss.Reaver)
      && With.self.minerals > Protoss.Scarab.mineralPrice
      && unit.scarabCount < maxScarabCount
      && unit.scarabCount < (if(unit.matchups.targetsInRange.isEmpty || unit.cooldownLeft > 0) maxScarabCount else 1)
      && unit.trainingQueue.size < 2
      && unit.trainee.forall(_.remainingCompletionFrames < With.reaction.agencyMax)
  )
  
  override def perform(unit: FriendlyUnitInfo) {
    With.commander.buildScarab(unit)
  }
}
