package ProxyBwapi.UnitTracking

import Lifecycle.With
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable

final class ScarabTracker {
  private val byReaver = mutable.Map.empty[Int, UnitInfo]

  def launchedBy(reaver: UnitInfo): Option[UnitInfo] = byReaver.get(reaver.id).filter(_.alive)

  def update(units: Iterable[FriendlyUnitInfo]): Unit = {
    byReaver.retain((_, scarab) => scarab.alive)
    val launched = units.filter(u => Protoss.Scarab(u) && u.frameDiscovered == With.frame)
    if (launched.isEmpty) return

    // BWAPI exposes launched Scarabs, but not their parent. Match only an unambiguous
    // launch: a fresh firing cooldown (not the 30-frame landing cooldown), within
    // 32 pixels of the Scarab's spawn point, which is 25 pixels ahead of its Reaver.
    val firing = units.filter(u =>
      Protoss.Reaver(u) && ! u.loaded && u.lastFrameStartingAttack == With.frame && u.cooldownRaw > 30)
    launched.foreach(scarab => {
      val candidates = firing.filter(_.pixel.pixelDistanceSquared(scarab.pixel) <= 32 * 32).toVector
      if (candidates.size == 1) byReaver(candidates.head.id) = scarab
    })
  }
}
