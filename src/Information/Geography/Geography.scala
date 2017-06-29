package Information.Geography

import Information.Geography.Calculations.{ZoneBuilder, ZoneUpdater}
import Information.Geography.Types.{Base, Zone, ZoneEdge}
import Lifecycle.With
import Mathematics.Points.{SpecificPoints, Tile, TileRectangle}
import Performance.Caching.{Cache, Limiter}
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable
import scala.collection.JavaConverters._

class Geography {
  
  val mapArea             : TileRectangle           = TileRectangle(Tile(0, 0), Tile(With.mapWidth, With.mapHeight))
  val allTiles            : Iterable[Tile]          = mapArea.tiles
  lazy val startLocations : Iterable[Tile]          = With.game.getStartLocations.asScala.map(new Tile(_))
  lazy val zones          : Iterable[Zone]          = ZoneBuilder.build
  def bases               : Iterable[Base]          = zones.flatten(_.bases)
  def ourZones            : Iterable[Zone]          = zones.filter(_.owner.isUs)
  def ourBases            : Iterable[Base]          = ourZones.flatten(_.bases)
  def enemyZones          : Iterable[Zone]          = zones.filterNot(zone => Vector(With.self, With.neutral).contains(zone.owner))
  def enemyBases          : Iterable[Base]          = enemyZones.flatten(_.bases)
  def ourTownHalls        : Iterable[UnitInfo]      = ourBases.flatMap(_.townHall)
  def ourHarvestingAreas  : Iterable[TileRectangle] = ourBases.map(_.harvestingArea)
  lazy val ourNatural     : Option[Base]            = bases.find(_.isNaturalOf.exists(_.owner.isUs))
  
  def zoneByTile(tile: Tile): Zone = zonesByTileCache(tile)
  private lazy val zonesByTileCache =
    new mutable.HashMap[Tile, Zone] {
      override def default(key: Tile): Zone = {
        val zone = zones.find(_.tiles.contains(key))
          .getOrElse(zones.minBy(_.centroid.tileDistanceSquared(key)))
        put(key, zone)
        zone
      }
    }
  
  def home: Tile = homeCache.get
  private val homeCache = new Cache(5, () =>
    ourBases
      .toVector
      .sortBy( ! _.isStartLocation)
      .headOption
      .map(_.townHallArea.startInclusive)
      .getOrElse(SpecificPoints.tileMiddle))
  
  def ourExposedChokes: Iterable[ZoneEdge] =
    With.geography.zones
      .filter(zone => zone.owner.isUs || zone.bases.exists(_.planningToTake))
      .flatten(_.edges)
      .filter(edge => edge.zones.exists( ! _.owner.isUs))
  
  def mostExposedChokes: Vector[ZoneEdge] =
    ourExposedChokes
      .toVector
      .sortBy(_.centerPixel.groundPixels(With.intelligence.mostBaselikeEnemyTile))
  
  def update() {
    zoneUpdateLimiter.act()
    bases.filter(base => With.game.isVisible(base.townHallArea.midpoint.bwapi)).foreach(base => base.lastScoutedFrame = With.frame)
  }
  
  private val zoneUpdateLimiter = new Limiter(2, () => ZoneUpdater.update())
}
