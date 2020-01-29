package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Transportation.RequestSafeLanding
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ReloadScarabs extends Action {
  
  val maxScarabCount = 4
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.is(Protoss.Reaver)
    && With.self.minerals >= Protoss.Scarab.mineralPrice
  )

  override def perform(unit: FriendlyUnitInfo) {

    val scarabGoal = if(unit.matchups.targetsInRange.isEmpty || unit.cooldownLeft > With.reaction.agencyMax) maxScarabCount else 1
    val scarabsNow = unit.scarabCount + unit.trainingQueue.size
    val needRefill = scarabsNow < scarabGoal && (
      // Refill while riding
      unit.agent.ride.isDefined
      // Don't queue up Scarabs
      || unit.trainingQueue.isEmpty
      || (unit.trainingQueue.nonEmpty && unit.trainee.forall(_.remainingCompletionFrames < With.reaction.agencyMax))
    )

    // Are we on ground, or safely able to
    val canRefill = unit.transport.isEmpty || unit.matchups.framesOfSafety > 24 + With.reaction.agencyMax

    if (needRefill && canRefill) {
      if (unit.transport.isDefined) {
        RequestSafeLanding().consider(unit)
      } else {
        With.commander.buildScarab(unit)
      }
    }
  }
}
