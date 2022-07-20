package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import Performance.Cache
import ProxyBwapi.Races.Protoss
import Utilities.UnitCounters.CountEverything
import Utilities.UnitFilters.IsMobileDetector

class SquadDarkTemplar extends Squad {
  lock.matcher = Protoss.DarkTemplar
  lock.counter = CountEverything

  override def launch(): Unit = {
    if (bases().isEmpty) return
    lock.acquire()
  }

  private val bases = new Cache(() =>
    With.geography.bases
      .filterNot(_.owner.isUs)
      .filterNot(_.metro.units.exists(u => u.isEnemy && IsMobileDetector(u)))
      .filterNot(_.enemies.exists(u =>
        u.complete
        && u.unitClass.isDetector
        && u.zone.exitNow.exists(_.sidePixels.exists(u.pixelDistanceCenter(_) <= u.sightPixels + 64)))))

  def run(): Unit = {
    if (bases().isEmpty) { lock.release(); return }
    val basesSorted = bases()
      .sortBy(_.heart.center.groundPixels(centroidGround))
      .sortBy( - With.scouting.baseIntrigue(_))
      .sortBy( ! _.owner.isEnemy)
    val base = basesSorted.head
    vicinity = base.heart.center
    targets = Some(base.enemies.toVector)

    if ( ! base.owner.isEnemy) {
      val divisions = With.battles.divisions.filter(d => d.enemies.exists( ! _.flying) && ! d.enemies.exists(_.unitClass.isDetector))
      Maff.minBy(divisions)(_.centroidGround.groundPixels(centroidGround)).foreach(division => {
        vicinity = division.centroidGround
        targets = Some(division.enemies.toVector)
      })
    }

    units.foreach(_.intend(this, new Intention { toTravel = Some(vicinity) }))
  }
}
