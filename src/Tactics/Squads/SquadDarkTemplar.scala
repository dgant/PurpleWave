package Tactics.Squads

import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import Performance.Cache
import Planning.UnitCounters.CountEverything
import Planning.UnitMatchers.MatchMobileDetector
import ProxyBwapi.Races.Protoss
import Utilities.Seconds

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
      .filterNot(_.metro.units.exists(u => u.isEnemy && MatchMobileDetector(u)))
      .filterNot(_.units.exists(u => u.isEnemy && u.complete && u.unitClass.isDetector && u.zone.exitNow.exists(_.sidePixels.exists(u.pixelDistanceCenter(_) <=  u.sightPixels)))),
    24)

  def run(): Unit = {
    if (bases().isEmpty) { lock.release(); return }
    val basesSorted = bases()
      .sortBy(_.heart.center.groundPixels(centroidGround))
      .sortBy(b => With.framesSince(b.lastScoutedFrame) < Seconds(30)())
      .sortBy( ! _.owner.isEnemy)
    val base = basesSorted.head
    vicinity = base.heart.center
    if ( ! base.owner.isEnemy) {
      val divisions = With.battles.divisions.filter(d => d.enemies.exists( ! _.flying) && ! d.enemies.exists(_.unitClass.isDetector))
      Maff.minBy(divisions)(_.centroidGround.groundPixels(centroidGround)).foreach(b => vicinity = b.centroidGround)
    }
    units.foreach(_.intend(this, new Intention { toTravel = Some(vicinity) }))
  }
}
