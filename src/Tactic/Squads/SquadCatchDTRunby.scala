package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import Performance.Cache
import Planning.Predicates.MacroFacts
import ProxyBwapi.Races.Protoss
import Utilities.UnitCounters.CountOne
import Utilities.UnitFilters.IsMobileDetector
import Utilities.UnitPreferences.PreferClose

class SquadCatchDTRunby extends Squad {

  def launch(): Unit = {
    if ( ! With.enemies.exists(_.isProtoss)) return
    if ( ! MacroFacts.enemyDarkTemplarLikely) return
    lock.matcher = IsMobileDetector
    lock.counter = CountOne
    lock.preference = PreferClose(destination())
    lock.acquire()
  }

  override def run(): Unit = {
    units.foreach(_.intend(this).setTravel(destination()))
  }

  private val destination = new Cache(() => {
    val dts = With.units.enemy.view.filter(Protoss.DarkTemplar)
    Maff.minBy(With.geography.ourBases.map(_.heart.center))(heart =>
      Maff.min(dts.map(_.pixelDistanceCenter(heart)))
        .getOrElse(heart.pixelDistance(With.scouting.enemyHome.center)))
      .getOrElse(With.geography.home.center)
    })
}
