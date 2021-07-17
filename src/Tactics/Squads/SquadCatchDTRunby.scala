package Tactics.Squads

import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import Performance.Cache
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers.MatchMobileDetector
import Planning.UnitPreferences.PreferClose
import ProxyBwapi.Races.Protoss

class SquadCatchDTRunby extends Squad {

  def launch(): Unit = {
    lock.matcher = MatchMobileDetector
    lock.counter = CountOne
    lock.preference = PreferClose(destination())
    addUnits(lock.acquire(this))
  }

  override def run(): Unit = {
    units.foreach(_.intend(this, new Intention { toTravel = Some(destination())}))
  }

  private val destination = new Cache(() => {
    val dts = With.units.enemy.view.filter(Protoss.DarkTemplar)
    Maff.minBy(With.geography.ourBases.map(_.heart.center))(heart =>
      Maff.min(dts.map(_.pixelDistanceCenter(heart)))
        .getOrElse(heart.pixelDistance(With.scouting.mostBaselikeEnemyTile.center)))
      .getOrElse(With.geography.home.center)
    })
}
