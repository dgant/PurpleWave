package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo}
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Utilities.UnitCounters.CountEverything
import Utilities.UnitFilters.IsTank

class SquadMindControl extends Squad {

  lock.matcher = Protoss.DarkArchon
  lock.counter = CountEverything

  def launch(): Unit = {
    if ( ! With.self.hasTech(Protoss.MindControl)) return
    if (stealables.isEmpty) return
    lock.acquire()
  }

  def stealables: Iterable[ForeignUnitInfo] = With.units.enemy.filter(_.isAny(
    Terran.Battlecruiser,
    IsTank,
    Protoss.Carrier,
    Protoss.Shuttle,
    Protoss.Reaver,
    Zerg.Guardian,
    Zerg.Ultralisk,
    Zerg.Lurker))

  override def run(): Unit = {
    if (units.isEmpty) return
    setTargets(stealables.toVector.sortBy(_.pixelDistanceTravelling(centroidGround)))
    val targetCenter = Maff.centroid(targets.get.view.map(_.pixel) :+ centroidGround)
    def isReady(unit: FriendlyUnitInfo): Boolean = targets.exists(_.nonEmpty) && unit.energy >= 150
    val ready = units.filter(isReady).sortBy(_.pixelDistanceTravelling(targetCenter.walkableTile))
    val unready = new UnorderedBuffer[FriendlyUnitInfo]
    unready.addAll(units.filterNot(isReady))

    var i = 0
    ready.foreach(darchon => {
      if (targets.get.size <= i ) {
        unready.add(darchon)
      } else {
        val destination = Some(targets.head(i).pixel.walkablePixel)
        darchon.intend(this).setTerminus(destination)
      }
      i += 1
    })

    lazy val safety = Maff.maxBy(With.geography.ourBases)(_.heart.tileDistanceSquared(With.scouting.enemyThreatOrigin)).map(_.heart).getOrElse(With.geography.home).center
    unready.foreach(_.intend(this).setTerminus(safety))
  }
}
