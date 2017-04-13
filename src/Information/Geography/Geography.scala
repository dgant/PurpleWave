package Information.Geography

import Information.Geography.Calculations.{ZoneBuilder, ZoneUpdater}
import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Mathematics.Pixels
import Mathematics.Pixels.{Tile, TileRectangle}
import Performance.Caching.{Cache, Limiter}
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class Geography {
  
  val mapArea             : TileRectangle           = new TileRectangle(new Tile(0, 0), new Tile(With.mapWidth, With.mapHeight))
  val allTiles            : Iterable[Tile]          = mapArea.tiles
  lazy val zones          : Iterable[Zone]          = ZoneBuilder.build
  def bases               : Iterable[Base]          = zones.flatten(_.bases)
  def ourZones            : Iterable[Zone]          = zones.filter(_.owner == With.self)
  def ourBases            : Iterable[Base]          = ourZones.flatten(_.bases)
  def enemyZones          : Iterable[Zone]          = zones.filterNot(zone => Vector(With.self, With.neutral).contains(zone.owner))
  def enemyBases          : Iterable[Base]          = enemyZones.flatten(_.bases)
  def ourTownHalls        : Iterable[UnitInfo]      = ourBases.flatMap(_.townHall)
  def ourHarvestingAreas  : Iterable[TileRectangle] = ourBases.map(_.harvestingArea)
  
  def zoneByTile(tile:Tile):Zone = zonesByTileCache(tile)
  private lazy val zonesByTileCache =
    new mutable.HashMap[Tile, Zone] {
      override def default(key: Tile): Zone = {
        val zone = zones
          .filter(_.tiles.contains(key))
          .headOption
          .getOrElse(zones.minBy(_.centroid.pixelDistanceFast(key.pixelCenter)))
        put(key, zone)
        zone
      }
    }
  
  def home:Tile = homeCache.get
  private val homeCache = new Cache(5, () =>
    ourBases
      .toVector
      .sortBy( ! _.isStartLocation)
      .headOption
      .map(_.townHallArea.startInclusive)
      .getOrElse(Pixels.Points.tileMiddle))
  
  def update() {
    zoneUpdateLimiter.act()
    bases.filter(base => With.game.isVisible(base.townHallArea.midpoint.bwapi)).foreach(base => base.lastScoutedFrame = With.frame)
  }
  
  private val zoneUpdateLimiter = new Limiter(2, () => ZoneUpdater.update())
}
