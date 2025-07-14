package Tactic.Tactics

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Points}
import Mathematics.Shapes.Circle
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Tactic.Assignment
import Utilities.UnitCounters.CountEverything

import scala.collection.mutable.ListBuffer

class TacticOverlords extends Tactic {
  
  lock
    .setMatcher(Zerg.Overlord)
    .setCounter(CountEverything)

  private val available: ListBuffer[FriendlyUnitInfo] = new ListBuffer[FriendlyUnitInfo]()

  lazy val zones: Vector[Zone] = With.geography.zones.sortBy(_.tiles.size)

  def launch(): Unit = {
    if ( ! With.self.isZerg) return

    available.clear()
    available ++= lock.acquire()

    val squads = With.squads.next.sortBy( - _.units.length)

    Assignment.squadsPick(
      available,
      squads,
      minimumValue = 1.0)

    With.geography.ourBasesAndSettlements
      .sortBy(_.frameTaken)
      .map(_.townHallArea.center)
      .foreach(assign)

    (With.geography.preferredExpansionsOurs.headOption ++ With.geography.preferredExpansionsEnemy)
      .toVector
      .distinct
      .map(base =>
        base.peak
          .map(_.center)
          .getOrElse(Points.middle.project(base.townHallArea.center, Zerg.Overlord.sightPixels).clamp()))
      .foreach(assign)

    With.geography.ourBases
      .flatMap(_.peak.map(_.center))
      .foreach(assign)

    zones
      .flatMap(_.peak.map(_.center))
      .foreach(assign)

    Assignment.unitsPick(
      available,
      squads)
  }

  def assign(pixel: Pixel): Unit = {
    if (available.isEmpty) return
    val overlord = available.minBy(_.pixelDistanceCenter(pixel))
    available -= overlord
    overlord.intend(this).setTerminus(pixel)
  }
  
  private def chillOut(overlord: FriendlyUnitInfo, count: Int): Unit = {
    val base = Maff.minBy(With.geography.ourBases.map(_.heart.center))(overlord.pixelDistanceSquared)
    val tile = base.map(b => Maff.sample(Circle(Math.sqrt(count).toInt).map(b.tile.add))).getOrElse(With.geography.home)
    overlord.intend(this).setTerminus(tile.center)
  }
}
