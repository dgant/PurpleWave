package Information.Geography

import Information.Geography.Calculations.{BuildZones, UpdateZones}
import Information.Geography.Types.{Base, Edge, Metro, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{SpecificPoints, Tile, TileRectangle}
import Performance.Tasks.TimedTask
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.JavaConverters._

class Geography extends TimedTask with GeographyCache {
  lazy val allTiles           : Array[Tile]           = new TileRectangle(0, 0, With.mapTileWidth, With.mapTileHeight).tiles.toArray
  lazy val startBases         : Vector[Base]          = bases.filter(_.isStartLocation)
  lazy val startLocations     : Vector[Tile]          = With.game.getStartLocations.asScala.map(new Tile(_)).toVector
  lazy val zones              : Vector[Zone]          = BuildZones.zones.toVector
  lazy val edges              : Vector[Edge]          = BuildZones.edges.toVector
  lazy val bases              : Vector[Base]          = BuildZones.bases.toVector
  lazy val metros             : Vector[Metro]         = BuildZones.metros.toVector
  lazy val ourMain            : Base                  = With.geography.ourBases.find(_.isStartLocation).getOrElse(With.geography.bases.minBy(_.heart.tileDistanceFast(With.self.startTile)))
  lazy val ourMetro           : Metro                 = ourMain.metro
  lazy val rushDistances      : Vector[Double]        = startLocations.flatMap(s1 => startLocations.filterNot(s1==).map(s2 => s1.groundPixels(s2))).sorted
  lazy val clockwiseBases     : Vector[Base]          = With.geography.bases.sortBy(b => SpecificPoints.middle.radiansTo(b.townHallArea.center))
  lazy val counterwiseBases   : Vector[Base]          = clockwiseBases.reverse

  var home: Tile = new Tile(With.game.self.getStartLocation)

  def ourNatural              : Base                  = ourNaturalCache()
  def ourZones                : Vector[Zone]          = ourZonesCache()
  def ourBases                : Vector[Base]          = ourBasesCache()
  def ourBasesAndSettlements  : Vector[Base]          = (ourBases ++ ourSettlementsCache()).distinct
  def ourTownHalls            : Vector[UnitInfo]      = ourTownHallsCache()
  def enemyZones              : Vector[Zone]          = enemyZonesCache()
  def enemyBases              : Vector[Base]          = enemyBasesCache()
  def neutralBases            : Vector[Base]          = bases.filter(_.owner.isNeutral)

  def zoneByTile(tile: Tile): Zone = if (tile.valid) zoneByTileCacheValid(tile.i) else zoneByTileCacheInvalid(tile)
  def baseByTile(tile: Tile): Option[Base] = if (tile.valid) baseByTileCacheValid(tile.i) else Maff.minBy(zoneByTileCacheInvalid(tile).bases)(_.heart.tileDistanceSquared(tile))

  def itinerary(start: Base, end: Base): Iterable[Base] = if (Maff.normalizePiToPi(Maff.radiansTo(start.radians, end.radians)) > 0) itineraryClockwise(start, end) else itineraryCounterwise(start, end)
  def itineraryClockwise    (start: Base, end: Base): Iterable[Base] = Maff.itinerary(start, end, clockwiseBases)
  def itineraryCounterwise  (start: Base, end: Base): Iterable[Base] = Maff.itinerary(start, end, counterwiseBases)

  override def onRun(budgetMs: Long): Unit = {
    if (With.frame == 0) {
      With.grids.walkableTerrain.initialize()
      With.grids.walkableTerrain.update()
      With.grids.unwalkableUnits.initialize()
      With.grids.unwalkableUnits.update()
    }
    UpdateZones.apply()
    zones.foreach(_.distanceGrid.initialize())
    zones.foreach(_.edges.foreach(_.distanceGrid.initialize()))
  }
}
