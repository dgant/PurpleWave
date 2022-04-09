package Tactic.Tactics

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Predicates.MacroFacts
import Planning.ResourceLocks.LockUnits
import Utilities.UnitCounters.CountOne
import Utilities.UnitFilters.IsMobileDetector
import Utilities.UnitPreferences.PreferClose
import ProxyBwapi.Races.{Protoss, Terran}

class MonitorTerranWithObserver extends Tactic {
  val scouts = new LockUnits(this)
  scouts.matcher = Protoss.Observer
  scouts.counter = CountOne
  scouts.interruptable = false

  override def launch(): Unit = {
    val bases = With.geography.enemyBases.filter(_.owner.isTerran)
    if (bases.isEmpty) return
    if (With.units.existsEnemy(IsMobileDetector)) return
    if (With.units.countEnemy(Terran.Factory) > 7) return
    if (With.enemies.exists(_.hasTech(Terran.WraithCloak))) return
    if (With.units.countOurs(Protoss.Observer) < 3 && MacroFacts.enemyHasShown(Terran.SpiderMine)) return
    scouts.preference = PreferClose(scouts.units.headOption.map(_.pixel).getOrElse(With.geography.home.center))
    scouts.acquire()
    scouts.units.foreach(scout => scout.intend(this, new Intention {
      toTravel = Some(With.geography.home.center)
      toScoutTiles = bases.flatMap(_.zone.tiles)
    }))
  }
}