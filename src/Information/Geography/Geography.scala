package Information.Geography

import Information.Geography.Calculations.{ZoneBuilder, ZoneUpdater}
import Information.Geography.Types.{Base, Edge, Metro, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{SpecificPoints, Tile, TileRectangle}
import Mathematics.Shapes.Spiral
import Performance.Cache
import Performance.Tasks.TimedTask
import Utilities.UnitMatchers.MatchTank
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.JavaConverters._
import scala.collection.mutable

class Geography extends TimedTask {
  lazy val mapArea            : TileRectangle         = TileRectangle(Tile(0, 0), Tile(With.mapTileWidth, With.mapTileHeight))
  lazy val allTiles           : Array[Tile]           = mapArea.tiles.indices.map(new Tile(_)).toArray
  lazy val startBases         : Vector[Base]          = bases.filter(_.isStartLocation)
  lazy val startLocations     : Vector[Tile]          = With.game.getStartLocations.asScala.map(new Tile(_)).toVector
  lazy val zones              : Vector[Zone]          = ZoneBuilder.zones.toVector
  lazy val edges              : Vector[Edge]          = ZoneBuilder.edges.toVector
  lazy val bases              : Vector[Base]          = ZoneBuilder.bases.toVector
  lazy val metros             : Vector[Metro]         = ZoneBuilder.metros.toVector
  lazy val ourMain            : Base                  = With.geography.ourBases.find(_.isStartLocation).getOrElse(With.geography.bases.minBy(_.heart.tileDistanceFast(With.self.startTile)))
  lazy val ourMetro           : Metro                 = ourMain.metro
  lazy val rushDistances      : Vector[Double]        = startLocations.flatMap(s1 => startLocations.filterNot(s1==).map(s2 => s1.groundPixels(s2))).sorted
  lazy val clockwiseBases     : Vector[Base]          = With.geography.bases.sortBy(b => SpecificPoints.middle.radiansTo(b.townHallArea.center))
  lazy val counterwiseBases   : Vector[Base]          = clockwiseBases.reverse

  def ourNatural              : Base                  = ourNaturalCache()
  def ourZones                : Vector[Zone]          = ourZonesCache()
  def ourBases                : Vector[Base]          = ourBasesCache()
  def ourUpcomingBases        : Vector[Base]          = ourUpcomingBasesCache()
  def ourBasesAndSettlements  : Vector[Base]          = (ourBases ++ ourSettlementsCache()).distinct
  def ourTownHalls            : Vector[UnitInfo]      = ourTownHallsCache()
  def ourHarvestingAreas      : Vector[TileRectangle] = ourHarvestingAreasCache()
  def ourBorder               : Vector[Edge]          = ourBorderCache()
  def enemyZones              : Vector[Zone]          = enemyZonesCache()
  def enemyBases              : Vector[Base]          = enemyBasesCache()
  def neutralBases            : Vector[Base]          = With.geography.bases.filter(_.owner.isNeutral)

  def zoneByTile(tile: Tile): Zone = if (tile.valid) zoneByTileCacheValid(tile.i) else zoneByTileCacheInvalid(tile)
  def baseByTile(tile: Tile): Option[Base] = if (tile.valid) baseByTileCacheValid(tile.i) else Maff.minBy(zoneByTileCacheInvalid(tile).bases)(_.heart.tileDistanceSquared(tile))

  def itinerary(start: Base, end: Base): Iterable[Base] = {
    val radians = Maff.normalizePiToPi(Maff.radiansTo(start.radians, end.radians))
    if (radians > 0) itineraryClockwise(start, end) else itineraryCounterwise(start, end)
  }

  def itineraryClockwise(start: Base, end: Base): Iterable[Base] = Maff.itinerary(start, end, clockwiseBases)
  def itineraryCounterwise(start: Base, end: Base): Iterable[Base] = Maff.itinerary(start, end, counterwiseBases)

  override def onRun(budgetMs: Long) {
    if (With.frame == 0) {
      With.grids.walkableTerrain.initialize()
      With.grids.walkableTerrain.update()
      With.grids.unwalkableUnits.initialize()
      With.grids.unwalkableUnits.update()
    }
    ZoneUpdater.update()
    zones.foreach(_.distanceGrid.initialize())
    zones.foreach(_.edges.foreach(_.distanceGrid.initialize()))
    bases.filter(base => With.game.isVisible(base.townHallArea.midpoint.bwapi)).foreach(base => base.lastScoutedFrame = With.frame)
  }

  var home: Tile = SpecificPoints.tileMiddle
  var naturalsSearched: Boolean = false
  
  private val ourZonesCache           = new Cache(() => zones.filter(_.owner.isUs))
  private val ourBasesCache           = new Cache(() => bases.filter(_.owner.isUs))
  private val ourUpcomingBasesCache   = new Cache(() => getUpcomingBases)
  private val ourSettlementsCache     = new Cache(() => getSettlements)
  private val enemyZonesCache         = new Cache(() => zones.filter(_.owner.isEnemy))
  private val enemyBasesCache         = new Cache(() => bases.filter(_.owner.isEnemy))
  private val ourTownHallsCache       = new Cache(() => ourBases.flatMap(_.townHall))
  private val ourHarvestingAreasCache = new Cache(() => ourBases.map(_.harvestingArea))
  private val ourBorderCache          = new Cache(() => ourZones.flatMap(_.edges).filter(_.zones.exists( ! _.owner.isFriendly)))
  private val ourNaturalCache = new Cache(() =>
    (if (ourMain.owner.isUs) ourMain.natural else None)
      .getOrElse(bases.find(_.isNaturalOf.exists(_.owner.isUs))
      .getOrElse(bases.minBy(_.townHallTile.groundPixels(ourMain.townHallTile)))))

  private lazy val zoneByTileCacheValid = allTiles.map(tile => zones.find(_.tiles.contains(tile)).getOrElse(getZoneForTile(tile)))
  private lazy val baseByTileCacheValid = allTiles.map(getBaseForTile)

  private val zoneByTileCacheInvalid = new mutable.HashMap[Tile, Zone] {
    override def default(key: Tile): Zone = {
      val zone: Zone = getZoneForTile(key)
      put(key, zone)
      zone
    }
  }
  private def getZoneForTile(tile: Tile): Zone =
    Maff
      .maxBy(
        Spiral
          .points(8)
          .map(point => {
            val neighbor = tile.add(point)
            if (neighbor.valid) zones.find(_.tiles.contains(neighbor)) else None
          })
          .filter(_.isDefined)
          .map(z => z.get)
          .groupBy(x => x))(_._2.size)
      .map(_._1)
      .getOrElse(zones.minBy(_.centroid.tileDistanceSquared(tile)))
  private def getBaseForTile(tile: Tile): Option[Base] = tile.zone.bases.find(_.tiles.contains(tile))

  private def getUpcomingBases: Vector[Base] = With.units.ours
    .view
    .filter(_.intent.toBuild.exists(_.isTownHall))
    .flatMap(_.intent.toBuildTile)
    .flatMap(_.base)
    .filter(_.townHall.isEmpty)
    .toVector

  private def getSettlements: Vector[Base] = (
    Vector.empty
    ++ With.geography.bases.view.filter(_.units.exists(u =>
        u.isOurs
        && u.unitClass.isBuilding
        && (u.unitClass.isTownHall || ! u.base.exists(_.townHallArea.intersects(u.tileArea)) // Ignore proxy base blockers
      )))
    ++ Vector(With.geography.ourNatural).filter(x =>
        With.strategy.isInverted
        && ! With.geography.ourMain.units.exists(_.unitClass.isStaticDefense)
        && With.units.ours.exists(u => u.complete && u.unitClass.ranged && (u.unitClass.canMove || u.is(MatchTank)))
        && (With.units.existsEnemy(_.unitClass.ranged) || With.battles.globalHome.judgement.exists(_.shouldFight)))
    ++ With.units.ours
      .view
      .filter(_.intent.toBuild.exists(_.isTownHall))
      .flatMap(_.intent.toBuildTile.map(tile => tile.zone.bases.find(base => base.townHallTile == tile)))
      .flatten
      .filterNot(_.owner.isUs)
    ).distinct
}
