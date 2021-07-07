package ProxyBwapi.UnitInfo

import Lifecycle.With
import ProxyBwapi.UnitTracking.Imagination
import bwapi.UnitType

final class ForeignUnitInfo(bwapiUnit: bwapi.Unit, id: Int) extends BWAPICachedUnitProxy(bwapiUnit, id) {

  override val foreign: Option[ForeignUnitInfo] = Some(this)

  override def update(): Unit = {
    if (frameDiscovered < With.frame) {
      readProxy()
    }
    super.update()
    Imagination.checkVisibility(this)
  }

  private def remainingFrames(snapshotHitPoints: Int, snapshotShields: Int, dataFrame: Int): Int = {
    val totalHealthInitial  = 1 + unitClass.maxTotalHealth / 10
    val totalHealthSnapshot = snapshotHitPoints + snapshotShields
    val progress            = Math.max(0.0, (totalHealthSnapshot - totalHealthInitial).toDouble / (unitClass.maxTotalHealth - totalHealthInitial))
    val progressLeft        = 1.0 - progress
    val output              = progressLeft * unitClass.buildFrames - With.framesSince(dataFrame)
    output.toInt
  }
  def remainingCompletionFrames: Int = {
    if (complete) return 0
    // Use both the initial projection and the up-to-date projection
    // We can't always trust the most up-to-date projection in case the unit has taken damage
    val remainingNow      = remainingFrames(hitPoints, shieldPoints, lastSeen)
    val remainingInitial  = remainingFrames(initialHitPoints, initialShields, frameDiscovered)
    val output            = Math.min(remainingNow, remainingInitial)
    output
  }

  // This check uses BWAPI unit type comparison because this unit's UnitClass isn't yet populated
  override val loadedUnitCount: Int = if (bwapiUnit.getType == UnitType.Protoss_Carrier) 8 else if (bwapiUnit.getType == UnitType.Terran_Bunker) 4 else 0
}
