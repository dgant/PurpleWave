package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import Performance.Cache
import Planning.Predicates.MacroFacts
import Utilities.UnitCounters.CountOne
import Utilities.UnitFilters.IsMobileDetector
import Utilities.UnitPreferences.PreferClose
import ProxyBwapi.Races.Protoss

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
    units.foreach(_.intend(this, new Intention { toTravel = Some(destination())}))
  }

  private val destination = new Cache(() => {
    val dts = With.units.enemy.view.filter(Protoss.DarkTemplar)
    Maff.minBy(With.geography.ourBases.map(_.heart.center))(heart =>
      Maff.min(dts.map(_.pixelDistanceCenter(heart)))
        .getOrElse(heart.pixelDistance(With.scouting.enemyHome.center)))
      .getOrElse(With.geography.home.center)
    })
}
