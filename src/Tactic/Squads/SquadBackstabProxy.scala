package Tactic.Squads

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Predicates.MacroFacts
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitFilters.{IsProxied, IsWarrior, IsWorker}
import Utilities.UnitPreferences.PreferClose

class SquadBackstabProxy extends Squad {

  lock.matcher = IsWarrior
  lock.counter = CountUpTo(2)
  lock.preference = PreferClose(vicinity)

  override def launch(): Unit = {
    vicinity = With.scouting.enemyHome.center
    if ( ! MacroFacts.enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.bbs)) return
    if (With.units.enemy.exists(IsProxied)) return
    if (lock.units.isEmpty) {
      if ( ! With.blackboard.wantToAttack()) return
      if (MacroFacts.unitsComplete(IsWarrior) < 7) return
    }
    lock.acquire()
  }

  override def run(): Unit = {
    targets = Some(With.units.enemy.filter(IsWorker).toVector)
    if (targets.isEmpty && With.geography.enemyBases.nonEmpty) targets = None
    units.foreach(_.intend(this, new Intention { toTravel = Some(vicinity) }))
  }
}
