package Tactic.Squads

import Lifecycle.With
import Planning.MacroFacts
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitFilters.{IsAny, IsWarrior, IsWorker}
import Utilities.UnitPreferences.PreferClose

class SquadBackstabProxy extends Squad {

  lock.matcher = IsAny(Terran.Vulture, Protoss.Zealot, Protoss.DarkTemplar, Zerg.Zergling, Zerg.Mutalisk)
  lock.counter = CountUpTo(2)

  override def launch(): Unit = {
    if ( ! MacroFacts.enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.proxyRax)) return
    if ( ! With.units.enemy.exists(_.proxied)) return
    if (With.geography.enemyBases.exists(_.units.exists(IsWarrior))) return

    val targetBase = With.scouting.enemyMain
    if (targetBase.isEmpty) return

    if (lock.units.isEmpty) {
      if ( ! With.blackboard.wantToAttack()) return
      if (MacroFacts.unitsComplete(IsWarrior) < 7) return
    }

    vicinity = targetBase.get.heart.center
    lock.setPreference(PreferClose(vicinity)).acquire()
  }

  override def run(): Unit = {
    setTargets(With.units.enemy.filter(IsWorker).toVector)
    if (targets.exists(_.isEmpty)) targets = None
    units.foreach(_.intend(this).setTerminus(vicinity))
  }
}
