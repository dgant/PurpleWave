package Information.Geography

import Information.Geography.Calculations.UpdateZones
import Information.Geography.Types.{Base, Metro, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Points, Tile}
import Performance.Tasks.TimedTask
import Planning.MacroFacts

import scala.collection.JavaConverters._

final class Geography extends TimedTask with GeographyCache with Expansions {
  lazy val mains              : Vector[Base] = bases.filter(_.isMain)
  lazy val startLocations     : Vector[Tile] = With.game.getStartLocations.asScala.map(new Tile(_)).toVector
  lazy val rushDistances      : Vector[Int]   = startLocations.flatMap(s1 => startLocations.filterNot(s1==).map(s1.groundPixelsBidirectional).map(_.toInt)).sorted
  lazy val clockwiseBases     : Vector[Base]  = With.geography.bases.sortBy(b => Points.middle.radiansTo(b.townHallArea.center))
  lazy val counterwiseBases   : Vector[Base]  = clockwiseBases.reverse

  var home: Tile = new Tile(With.game.self.getStartLocation)

  def ourMain                 : Base          = ourMainCache()
  def ourNatural              : Base          = ourNaturalCache()
  def ourMetro                : Metro         = ourMain.metro
  def ourZones                : Vector[Zone]  = ourZonesCache()
  def ourBases                : Vector[Base]  = ourBasesCache()
  def ourMiningBases          : Vector[Base]  = ourBases.filter(MacroFacts.isMiningBase)
  def ourBasesAndSettlements  : Vector[Base]  = (ourBases ++ ourSettlementsCache()).distinct
  def enemyBases              : Vector[Base]  = enemyBasesCache()
  def enemyMiningBases        : Vector[Base]  = enemyBases.filter(MacroFacts.isMiningBase)
  def neutralBases            : Vector[Base]  = bases.filter(_.owner.isNeutral)

  def itinerary             (start: Base, end: Base): Iterable[Base] = if (Maff.normalizePiToPi(Maff.radiansTo(start.radians, end.radians)) > 0) itineraryClockwise(start, end) else itineraryCounterwise(start, end)
  def itineraryClockwise    (start: Base, end: Base): Iterable[Base] = Maff.itinerary(start, end, clockwiseBases)
  def itineraryCounterwise  (start: Base, end: Base): Iterable[Base] = Maff.itinerary(start, end, counterwiseBases)

  override def onRun(budgetMs: Long): Unit = {
    UpdateZones()
    updateExpansions()
  }
}
