package Tactic.Tactics

import Lifecycle.With
import Macro.Facts.MacroFacts
import Mathematics.Maff
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.Time.Seconds
import Utilities.UnitCounters.CountOne
import Utilities.UnitFilters.{IsAny, IsMobileDetector}
import Utilities.UnitPreferences.PreferClose

class TacticMonitor extends Tactic {

  val scouts: LockUnits = new LockUnits(this, IsAny(Protoss.Observer, Zerg.Overlord), CountOne, interruptable = false)

  override def launch(): Unit = {
    var bases = With.geography.enemyBases
    if (bases.isEmpty)                                      return
    if ( ! With.blackboard.monitorBases())                  return
    if (With.units.existsEnemy(IsMobileDetector))           return
    if (With.units.countEnemy(Terran.Factory) > 7)          return
    if (With.enemies.exists(_.hasTech(Terran.WraithCloak))) return
    if (With.units.countOurs(Protoss.Observer) < 3 && MacroFacts.enemyHasShown(Terran.SpiderMine)) return
    scouts
      .setPreference(PreferClose(scouts.units.headOption.map(_.pixel).getOrElse(With.geography.home.center)))
      .acquire()
    val cloaked = scouts.units.exists(_.cloaked)
    val naturals = bases.flatMap(_.natural.filter(b => With.framesSince(b.lastFrameScoutedByUs) > Seconds(25)()))
    bases = Maff.orElse(naturals, bases).toVector
    scouts.units.foreach(_.intend(this)
      .setTerminus(With.geography.home.center)
      .setScout(bases.flatMap(_.zone.tiles.filter(t => t.enemyRangeAir <= 0 && ( ! cloaked || ! t.enemyDetectedUnchecked)))))
  }
}
