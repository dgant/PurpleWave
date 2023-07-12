package ProxyBwapi.UnitInfo

import Lifecycle.With
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitTracking.{GhostUnit, Imagination}
import bwapi.UnitType

final class ForeignUnitInfo(bwapiUnit: bwapi.Unit, id: Int) extends BWAPICachedUnitProxy(bwapiUnit, id) {

  override val foreign: Option[ForeignUnitInfo] = Some(this)

  var lastImagination: Int = 0

  override def update(): Unit = {
    if (GhostUnit(this)) {
      With.units.onUnitDestroy(bwapiUnit)
      return
    }
    if (frameDiscovered < With.frame) {
      readProxy()
    }
    super.update()
    Imagination.checkVisibility(this)
    if ( ! visible && ! complete && remainingCompletionFrames <= 0) {
      changeCompletion(true)
    }
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
    if (morphing && isAny(Zerg.Lair, Zerg.Hive, Zerg.SunkenColony, Zerg.SporeColony, Zerg.GreaterSpire, Zerg.LurkerEgg, Zerg.Cocoon)) {
      val buildFrames = (
              if (Zerg.LurkerEgg(this)) Zerg.Lurker
        else  if (Zerg.Cocoon(this))    Zerg.Guardian
        else                            unitClass).buildFrames
      return Math.max(1, buildFrames + lastClassChange - With.frame)
    }
    // Use both the initial projection and the up-to-date projection
    // We can't always trust the most up-to-date projection in case the unit has taken damage
    val remainingNow      = remainingFrames(hitPoints,              shieldPoints,           lastSeen)
    val remainingInitial  = remainingFrames(lastClassChangeHealth,  lastClassChangeShields, lastClassChange)
    val output            = Math.min(remainingNow, remainingInitial)
    output
  }

  // This check uses BWAPI unit type comparison because this unit's UnitClass isn't yet populated
  override val loadedUnitCount: Int = if (bwapiUnit.getType == UnitType.Protoss_Carrier) 8 else if (bwapiUnit.getType == UnitType.Terran_Bunker) 4 else 0
}
