package Micro.Actions.Basic

import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import Micro.Actions.Transportation.RequestSafeLanding
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ReloadScarabs extends Action {
  
  val maxScarabCount = 5
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.is(Protoss.Reaver)
    && With.self.minerals >= Protoss.Scarab.mineralPrice
  )

  override def perform(unit: FriendlyUnitInfo): Unit = {
    val scarabGoal = if(unit.matchups.targetsInRange.isEmpty || unit.cooldownLeft > With.reaction.agencyMax) maxScarabCount else 1
    val scarabsNow = unit.scarabs + unit.trainingQueue.size
    val scarabsNeeded = scarabGoal - scarabsNow
    val needRefill = scarabsNeeded > 0 && (
      // Refill while riding
      unit.agent.ride.isDefined
      // Don't queue up Scarabs
      || unit.trainingQueue.isEmpty
      || (unit.trainingQueue.nonEmpty && unit.trainee.forall(_.remainingCompletionFrames < With.reaction.agencyMax))
    )
    // Are we on ground, or safely able to land
    val canRefill = With.self.minerals >= Math.min(45, 15 * scarabsNeeded) && (unit.transport.isEmpty || unit.matchups.framesOfSafety > 24 + With.reaction.agencyMax)
    if (needRefill && canRefill) {
      if (unit.airlifted) {
        With.logger.micro(f"$unit in ${unit.transport.get} landing to refill")
        RequestSafeLanding(unit)
      } else if (unit.bwapiUnit.getTrainingQueueCount <= Maff.fromBoolean(unit.remainingTrainFrames <= Math.max(With.reaction.agencyMax, With.latency.remainingFrames))) {
        Commander.buildScarab(unit)
      }
    }
  }
}
