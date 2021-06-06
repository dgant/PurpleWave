package Micro.Squads

import Lifecycle.With
import Micro.Agency.Intention
import Performance.Cache
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers.MatchMobileDetector
import Planning.UnitPreferences.PreferClose
import ProxyBwapi.Races.Protoss
import Utilities.ByOption

class SquadCatchDTRunby extends Squad {

  def recruit(): Unit = {
    lock.matcher = MatchMobileDetector
    lock.counter = CountOne
    lock.preference = PreferClose(destination())
    lock.acquire(this)
    addUnits(lock.units)
  }

  override def run(): Unit = {
    units.foreach(_.agent.intend(this, new Intention { toTravel = Some(destination())}))
  }

  private val destination = new Cache(() => {
    val dts = With.units.enemy.view.filter(Protoss.DarkTemplar)
    ByOption.minBy(With.geography.ourBases.map(_.heart.center))(heart =>
      ByOption.min(dts.map(_.pixelDistanceCenter(heart)))
        .getOrElse(heart.pixelDistance(With.scouting.mostBaselikeEnemyTile.center)))
      .getOrElse(With.geography.home.center)
    })
}
