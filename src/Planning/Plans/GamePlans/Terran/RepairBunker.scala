package Planning.Plans.GamePlans.Terran

import Lifecycle.With
import Mathematics.Maff
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.?
import Utilities.Time.Seconds
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitPreferences.PreferClose

class RepairBunker extends Plan {
  val lock = new LockUnits(this, Terran.SCV)

  override def onUpdate(): Unit = {
    val bunkers = With.units.ours
      .view
      .filter(u =>
        Terran.Bunker(u)
        && ! u.base.exists(_.owner.isEnemy)
        && u.matchups.framesOfSafety < (if (u.hitPoints < u.unitClass.maxHitPoints) 72 else 12)
        && u.remainingCompletionFrames < Seconds(5)())
      .toVector
      .sortBy(_.matchups.framesOfSafety)
      .sortBy(_.totalHealth)

    if (bunkers.isEmpty) return
    val bunker = bunkers.head

    val repairersNeeded = Maff.clamp(bunker.matchups.threats
      .map(t =>
        if ( ! t.visible)                 0.0
        else if (Terran.Marine(t))        0.5
        else if (Terran.Vulture(t))       0.5
        else if (Protoss.Zealot(t))       1.5
        else if (Protoss.Dragoon(t))      ?(t.player.hasUpgrade(Protoss.DragoonRange), 1.0, 0.0)
        else if (Protoss.DarkTemplar(t))  2.0
        else if (Zerg.Zergling(t))        0.25
        else if (Zerg.Hydralisk(t))       1.0
        else if (Zerg.Lurker(t))          1.0
        else                              0.0)
      .sum.toInt - 3 * bunker.alliesBattle.count(Terran.SiegeTankSieged),
      0,
      Math.min(6, With.units.countOurs(Terran.SCV) / 2 - 1))

    lock.release()
    lock
      .setCounter(CountUpTo(repairersNeeded))
      .setPreference(PreferClose(bunker.pixel))
      .acquire()
      .foreach(_.intend(this).setRepair(bunker))
  }
}
