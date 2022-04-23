package Information.Geography

import Information.Geography.Calculations.UpdateZones
import Information.Geography.Types.{Base, Metro, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{SpecificPoints, Tile}
import Performance.Tasks.TimedTask

import scala.collection.JavaConverters._

final class Geography extends TimedTask with GeographyCache with Expansions {
  lazy val startBases         : Vector[Base]    = bases.filter(_.isStartLocation)
  lazy val startLocations     : Vector[Tile]    = With.game.getStartLocations.asScala.map(new Tile(_)).toVector
  lazy val ourMain            : Base            = With.geography.ourBases.find(_.isStartLocation).getOrElse(With.geography.bases.minBy(_.heart.tileDistanceFast(With.self.startTile)))
  lazy val ourMetro           : Metro           = ourMain.metro
  lazy val rushDistances      : Vector[Double]  = startLocations.flatMap(s1 => startLocations.filterNot(s1==).map(s2 => s1.groundPixels(s2))).sorted
  lazy val clockwiseBases     : Vector[Base]    = With.geography.bases.sortBy(b => SpecificPoints.middle.radiansTo(b.townHallArea.center))
  lazy val counterwiseBases   : Vector[Base]    = clockwiseBases.reverse

  var home: Tile = new Tile(With.game.self.getStartLocation)

  def ourNatural              : Base            = ourNaturalCache()
  def ourZones                : Vector[Zone]    = ourZonesCache()
  def ourBases                : Vector[Base]    = ourBasesCache()
  def ourBasesAndSettlements  : Vector[Base]    = (ourBases ++ ourSettlementsCache()).distinct
  def enemyBases              : Vector[Base]    = enemyBasesCache()
  def neutralBases            : Vector[Base]    = bases.filter(_.owner.isNeutral)

  def itinerary             (start: Base, end: Base): Iterable[Base] = if (Maff.normalizePiToPi(Maff.radiansTo(start.radians, end.radians)) > 0) itineraryClockwise(start, end) else itineraryCounterwise(start, end)
  def itineraryClockwise    (start: Base, end: Base): Iterable[Base] = Maff.itinerary(start, end, clockwiseBases)
  def itineraryCounterwise  (start: Base, end: Base): Iterable[Base] = Maff.itinerary(start, end, counterwiseBases)

  override def onRun(budgetMs: Long): Unit = {
    UpdateZones.apply()
    updateExpansions()
  }
}
