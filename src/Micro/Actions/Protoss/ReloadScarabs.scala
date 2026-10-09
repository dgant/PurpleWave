package Micro.Actions.Protoss

import Lifecycle.With
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

  def readyForPickup(unit: FriendlyUnitInfo): Boolean =
    !Protoss.Reaver(unit) || unit.scarabs + unit.trainingQueue.size >= 2

  override def perform(unit: FriendlyUnitInfo): Unit = {
    val queued = Math.max(unit.trainingQueue.size, unit.bwapiUnit.getTrainingQueueCount)
    val needed = maxScarabCount - unit.scarabs - queued
    if (needed <= 0) return
    if (unit.airlifted) {
      // Queued Scarabs finish in flight; do not land just to top up a ready passenger.
      if (!readyForPickup(unit) && unit.matchups.framesOfSafety > 24 + With.reaction.agencyMax) {
        RequestSafeLanding(unit)
      }
    } else {
      Commander.buildScarab(unit, Math.min(needed, With.self.minerals / Protoss.Scarab.mineralPrice))
    }
  }
}
