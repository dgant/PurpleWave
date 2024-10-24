package Tactic.Squads

import Lifecycle.With
import Macro.Facts.MacroFacts
import Mathematics.Maff
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.Protoss
import Utilities.Time.Seconds
import Utilities.UnitCounters.{CountOne, CountUpTo}
import Utilities.UnitFilters.{IsAll, IsBuilding, IsMobileDetector, IsWarrior}
import Utilities.UnitPreferences.PreferClose

class SquadCatchDTs extends Squad {

  private lazy val lockWarriors: LockUnits = new LockUnits(this)
    .setMatcher(IsAll(IsWarrior, _.canAttackGround))
    .setCounter(CountUpTo(2));

  def launch(): Unit = {
    lazy val bases = With.geography.ourBases.filter(_.workerCount > 1)
    if ( ! With.enemies.exists(_.isProtoss))  return
    if ( ! MacroFacts.enemyDarkTemplarLikely) return
    if ( ! With.blackboard.wantToAttack())    return
    if (bases.isEmpty)                        return
    if (With.scouting.earliestArrival(Protoss.DarkTemplar) > With.frame + Seconds(15)()) return

    setEnemies(With.units.enemy.view.filter(Protoss.DarkTemplar))
    setTargets(enemies)

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
      lockWarriors.setPreference(PreferClose(vicinity)).acquire()
    }
  }

  override def run(): Unit = {
    val darkTemplar = enemies.filter(dt => dt.proximity > 0.5 || dt.metro.exists(_.isOurs))
    vicinity = Maff.minBy(darkTemplar.map(_.pixel))(_.groundPixels(vicinity)).getOrElse(vicinity)
    units.foreach(_.intend(this).setTerminus(vicinity))
  }
}
