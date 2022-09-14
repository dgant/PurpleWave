package Tactic.Tactics

import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import Planning.Predicates.MacroFacts
import Planning.ResourceLocks.LockUnits
import Utilities.UnitCounters.CountOne
import Utilities.UnitFilters.{IsAny, IsMobileDetector}
import Utilities.UnitPreferences.PreferClose
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.Time.Seconds

class Monitor extends Tactic {
  val scouts = new LockUnits(this)
  scouts.matcher = IsAny(Protoss.Observer, Zerg.Overlord)
  scouts.counter = CountOne
  scouts.interruptable = false

  override def launch(): Unit = {
    var bases = With.geography.enemyBases
    if (bases.isEmpty) return
    if ( ! With.blackboard.monitorBases()) return
    if (With.units.existsEnemy(IsMobileDetector)) return
    if (With.units.countEnemy(Terran.Factory) > 7) return
    if (With.enemies.exists(_.hasTech(Terran.WraithCloak))) return
    if (With.units.countOurs(Protoss.Observer) < 3 && MacroFacts.enemyHasShown(Terran.SpiderMine)) return
    scouts.preference = PreferClose(scouts.units.headOption.map(_.pixel).getOrElse(With.geography.home.center))
    scouts.acquire()
    val cloaked = scouts.units.exists(_.cloaked)
    val naturals = bases.flatMap(_.natural.filter(b => With.framesSince(b.lastFrameScoutedByUs) > Seconds(45)()))
    bases = Maff.orElse(naturals, bases).toVector
    scouts.units.foreach(scout => scout.intend(this, new Intention {
      toTravel = Some(With.geography.home.center)
      toScoutTiles = bases.flatMap(_.zone.tiles.filter(t => t.enemyRangeAir <= 0 && ( ! cloaked || ! t.enemyDetectedUnchecked)))
    }))
  }
}
