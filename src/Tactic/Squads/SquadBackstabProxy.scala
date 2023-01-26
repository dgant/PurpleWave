package Tactic.Squads

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Predicates.MacroFacts
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitFilters.{IsWarrior, IsWorker}
import Utilities.UnitPreferences.PreferClose

class SquadBackstabProxy extends Squad {

  lock.matcher = IsWarrior
  lock.counter = CountUpTo(2)
  lock.preference = PreferClose(vicinity)

  override def launch(): Unit = {
    if ( ! MacroFacts.enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.proxyRax)) return
    if ( ! With.units.enemy.exists(_.proxied)) return

    val targetBase = With.scouting.enemyMain
    if (targetBase.isEmpty) return

    if (lock.units.isEmpty) {
      if ( ! With.blackboard.wantToAttack()) return
      if (MacroFacts.unitsComplete(IsWarrior) < 7) return
    }

    vicinity = targetBase.get.heart.center
    lock.acquire()
  }

  override def run(): Unit = {
    setTargets(With.units.enemy.filter(IsWorker).toVector)
    if (targets.exists(_.isEmpty)) targets = None
    units.foreach(_.intend(this, new Intention { toTravel = Some(vicinity) }))
  }
}
