package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import Planning.MacroFacts
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.Protoss
import Utilities.UnitCounters.{CountOne, CountUpTo}
import Utilities.UnitFilters.{IsAll, IsBuilding, IsMobileDetector, IsWarrior}
import Utilities.UnitPreferences.PreferClose

class SquadCatchDTRunby extends Squad {

  private lazy val lockWarriors: LockUnits = new LockUnits(this);

  def launch(): Unit = {
    if ( ! With.enemies.exists(_.isProtoss))  return
    if ( ! MacroFacts.enemyDarkTemplarLikely) return
    setEnemies(With.units.enemy.view.filter(Protoss.DarkTemplar))
    setTargets(enemies)

    val bases = With.geography.ourBases.filter(_.workerCount > 1)
    if (bases.isEmpty) return

    vicinity = bases.map(_.heart.center).minBy(heart =>
      Maff.min(enemies.map(_.pixelDistanceCenter(heart)))
      .getOrElse(heart.pixelDistance(With.scouting.enemyHome.center)))

    val base  = bases.minBy(_.heart.groundTiles(vicinity))
    val tower = base.units.find(u => IsBuilding(u) && u.canAttackGround)

    lock
      .setMatcher(IsMobileDetector)
      .setCounter(CountOne)
      .setPreference(PreferClose(vicinity))
      .acquire()

    if (tower.isEmpty) {
      lockWarriors
        .setMatcher(IsAll(IsWarrior, u => u.canAttackGround))
        .setCounter(CountUpTo(2))
        .setPreference(PreferClose(vicinity))
        .acquire()
    }
  }

  override def run(): Unit = {
    setTargets(vicinity.zone.metro.map(_.zones).getOrElse(Seq(vicinity.zone)).flatMap(_.enemies.view.filter(Protoss.DarkTemplar)))
    units.foreach(_.intend(this).setTerminus(vicinity))
  }
}
