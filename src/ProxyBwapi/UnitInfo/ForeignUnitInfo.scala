package ProxyBwapi.UnitInfo

import Lifecycle.With
import Micro.Squads.Squad
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitTracking.Imagination

import scala.collection.mutable.ArrayBuffer

final class ForeignUnitInfo(bwapiUnit: bwapi.Unit, id: Int) extends BWAPICachedUnitProxy(bwapiUnit, id) {

  override val foreign: Option[ForeignUnitInfo] = Some(this)

  override def update(): Unit = {
    if (frameDiscovered < With.frame) {
      readProxy()
    }
    super.update()
    Imagination.checkVisibility(this)
  }

  private var _squads = new ArrayBuffer[Squad]()
  def enemyOfSquads: Seq[Squad] = _squads
  def clearSquads(): Unit = { _squads.clear() }
  def addSquad(squad: Squad): Unit = { _squads += squad }

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

  override val loadedUnitCount: Int = if (is(Protoss.Carrier)) 8 else if (is(Terran.Bunker)) 4 else 0
}
