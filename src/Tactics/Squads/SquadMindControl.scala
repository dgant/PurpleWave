package Tactics.Squads

import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import Planning.UnitCounters.CountEverything
import Planning.UnitMatchers.MatchTank
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo}
import ProxyBwapi.UnitTracking.UnorderedBuffer

class SquadMindControl extends Squad {

  lock.matcher = Protoss.DarkArchon
  lock.counter = CountEverything

  def launch(): Unit = {
    if ( ! With.self.hasTech(Protoss.MindControl)) return
    if (stealables.isEmpty) return
    addUnits(lock.acquire())
  }

  def stealables: Iterable[ForeignUnitInfo] = With.units.enemy.filter(_.isAny(
    Terran.Battlecruiser,
    MatchTank,
    Protoss.Carrier,
    Protoss.Shuttle,
    Protoss.Reaver,
    Zerg.Guardian,
    Zerg.Ultralisk,
    Zerg.Lurker))

  override def run(): Unit = {
    if (units.isEmpty) return
    targetQueue = Some(stealables.toVector.sortBy(_.pixelDistanceTravelling(centroidGround)))
    val targetCenter = Maff.centroid(targetQueue.get.view.map(_.pixel) :+ centroidGround)
    def isReady(unit: FriendlyUnitInfo): Boolean = targetQueue.exists(_.nonEmpty) && unit.energy >= 150
    val ready = units.filter(isReady).sortBy(_.pixelDistanceTravelling(targetCenter.nearestWalkableTile))
    val unready = new UnorderedBuffer[FriendlyUnitInfo]
    unready.addAll(units.filterNot(isReady))

    var i = 0
    ready.foreach(darchon => {
      if (targetQueue.get.size <= i ) {
        unready.add(darchon)
      } else {
        darchon.intend(this, new Intention { toTravel = Some(targetQueue.head(i).pixel.nearestWalkablePixel) })
      }
      i += 1
    })

    lazy val safety = Maff.maxBy(With.geography.ourBases)(_.heart.tileDistanceSquared(With.scouting.threatOrigin)).map(_.heart).getOrElse(With.geography.home).center
    unready.foreach(_.intend(this, new Intention { toTravel = Some(safety) }))
  }
}