package Planning.Plans.GamePlans.Terran.Situational

import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountUpTo
import Planning.UnitPreferences.PreferClose
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.Seconds

class RepairBunker extends Plan {
  val lock = new LockUnits(this)
  lock.matcher = Terran.SCV

  override def onUpdate(): Unit = {
    val bunkers = With.units.ours
      .view
      .filter(u =>
        u.is(Terran.Bunker)
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
        if ( ! t.visible)
          0.0
        else if (t.is(Terran.Marine))
          0.5
        else if (t.is(Protoss.Zealot))
          2.0
        else if (t.is(Protoss.Dragoon))
          (if (t.player.hasUpgrade(Protoss.DragoonRange)) 1.0 else 0.0)
        else if (t.is(Protoss.DarkTemplar))
          2.0
        else if (t.is(Zerg.Zergling))
          0.25
        else if (t.is(Zerg.Hydralisk))
          1.0
        else if (t.is(Zerg.Lurker))
          1.0
        else
          0.0)
      .sum.toInt - 3 * bunker.alliesBattle.count(Terran.SiegeTankSieged),
      0,
      Math.min(6, With.units.countOurs(Terran.SCV) / 2 - 1))

    lock.release()
    lock.counter = CountUpTo(repairersNeeded)
    lock.preference = PreferClose(bunker.pixel)
    lock.acquire(this)
    lock.units.foreach(scv => scv.intend(this, new Intention {
      toRepair = Some(bunker)
    }))
  }
}
