package Information.Geography

import Geometry._
import Information.Geography.Calculations.{ZoneBuilder, ZoneUpdater}
import Information.Geography.Types.{Base, Zone}
import Performance.Caching.{Cache, CacheForever, Limiter}
import ProxyBwapi.UnitInfo.UnitInfo
import Startup.With
import bwapi.TilePosition
import Utilities.EnrichPosition._

import scala.collection.mutable

class Geography {
  
  val mapArea             : TileRectangle           = new TileRectangle(new TilePosition(0, 0), new TilePosition(With.mapWidth, With.mapHeight))
  val allTiles            : Iterable[TilePosition]  = mapArea.tiles
  def zones               : Iterable[Zone]          = zoneCache.get
  def bases               : Iterable[Base]          = zones.flatten(_.bases)
  def ourZones            : Iterable[Zone]          = zones.filter(_.owner == With.self)
  def ourBases            : Iterable[Base]          = ourZones.flatten(_.bases)
  def enemyZones          : Iterable[Zone]          = zones.filterNot(zone => List(With.self, With.neutral).contains(zone.owner))
  def enemyBases          : Iterable[Base]          = enemyZones.flatten(_.bases)
  def ourTownHalls        : Iterable[UnitInfo]      = ourBases.flatMap(_.townHall)
  def ourHarvestingAreas  : Iterable[TileRectangle] = ourBases.map(_.harvestingArea)
  
  def zoneByTile(tile:TilePosition):Zone = zonesByTileCache.get.get(tile).getOrElse(zones.minBy(_.centroid.pixelDistance(tile.pixelCenter)))
  private val zonesByTileCache = new CacheForever(() => zonesByTileBuild)
  private def zonesByTileBuild:Map[TilePosition, Zone] = {
    val output = new mutable.HashMap[TilePosition, Zone]
    zones.foreach(zone => zone.tiles.foreach(tile => output.put(tile, zone)))
    output.toMap
  }
  
  def home:TilePosition = homeCache.get
  private val homeCache = new Cache(5, () =>
    ourBases
      .toList
      .sortBy( ! _.isStartLocation)
      .headOption
      .map(_.townHallRectangle.startInclusive)
      .getOrElse(Positions.tileMiddle))
  
  def onFrame() {
    zoneUpdateLimiter.act()
    bases.filter(base => With.game.isVisible(base.townHallRectangle.midpoint)).foreach(base => base.lastScoutedFrame = With.frame)
  }
  
  private val zoneCache         = new CacheForever(() => ZoneBuilder.build)
  private val zoneUpdateLimiter = new Limiter(2, () => ZoneUpdater.update())
}
