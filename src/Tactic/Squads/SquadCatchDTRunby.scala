package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import Planning.MacroFacts
import ProxyBwapi.Races.Protoss
import Utilities.UnitCounters.CountOne
import Utilities.UnitFilters.IsMobileDetector
import Utilities.UnitPreferences.PreferClose

class SquadCatchDTRunby extends Squad {

  def launch(): Unit = {
    if ( ! With.enemies.exists(_.isProtoss))  return
    if ( ! MacroFacts.enemyDarkTemplarLikely) return
    setEnemies(With.units.enemy.view.filter(Protoss.DarkTemplar))
    setTargets(enemies)
    val bases = With.geography.ourBases.filter(_.workerCount > 1)
    if (bases.isEmpty) return
    vicinity        = Maff.minBy(bases.map(_.heart.center))(heart => Maff.min(enemies.map(_.pixelDistanceCenter(heart))).getOrElse(heart.pixelDistance(With.scouting.enemyHome.center))).getOrElse(With.geography.home.center)
    lock
      .setMatcher(IsMobileDetector)
      .setCounter(CountOne)
      .setPreference(PreferClose(vicinity))
      .acquire()
  }

  override def run(): Unit = {
    setTargets(vicinity.zone.metro.map(_.zones).getOrElse(Seq(vicinity.zone)).flatMap(_.enemies.view.filter(Protoss.DarkTemplar)))
    units.foreach(_.intend(this).setTerminus(vicinity))
  }
}
