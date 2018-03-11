package Information.Geography

import Information.Geography.Calculations.{ZoneBuilder, ZoneUpdater}
import Information.Geography.Types.{Base, Edge, Zone}
import Lifecycle.With
import Mathematics.Points.{SpecificPoints, Tile, TileRectangle}
import Performance.Cache
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.JavaConverters._
import scala.collection.mutable

class Geography {
  lazy val mapArea            : TileRectangle           = TileRectangle(Tile(0, 0), Tile(With.mapTileWidth, With.mapTileHeight))
  lazy val allTiles           : Array[Tile]             = mapArea.tiles
  lazy val startBases         : Iterable[Base]          = bases.filter(_.isStartLocation)
  lazy val startLocations     : Iterable[Tile]          = With.game.getStartLocations.asScala.map(new Tile(_))
  lazy val zones              : Iterable[Zone]          = ZoneBuilder.zones
  lazy val edges              : Iterable[Edge]          = ZoneBuilder.edges
  lazy val bases              : Iterable[Base]          = ZoneBuilder.bases
  lazy val ourMain            : Base                    = With.geography.ourBases.find(_.isStartLocation).getOrElse(With.geography.bases.minBy(_.heart.tileDistanceFast(With.self.startTile)))
  def ourNatural              : Base                    = ourNaturalCache()
  def ourZones                : Iterable[Zone]          = ourZonesCache()
  def ourBases                : Iterable[Base]          = ourBasesCache()
  def ourSettlements          : Iterable[Base]          = ourSettlementsCache()
  def ourBasesAndSettlements  : Iterable[Base]          = (ourBases ++ ourSettlements).toSet.toVector
  def ourTownHalls            : Iterable[UnitInfo]      = ourTownHallsCache()
  def ourHarvestingAreas      : Iterable[TileRectangle] = ourHarvestingAreasCache()
  def ourBorder               : Iterable[Edge]          = ourBorderCache()
  def enemyZones              : Iterable[Zone]          = enemyZonesCache()
  def enemyBases              : Iterable[Base]          = enemyBasesCache()
  def neutralBases            : Iterable[Base]          = With.geography.bases.filter(_.owner.isNeutral)
  
  
  private val ourZonesCache           = new Cache(() => zones.filter(_.owner.isUs))
  private val ourBasesCache           = new Cache(() => bases.filter(_.owner.isUs))
  private val ourSettlementsCache     = new Cache(() => getSettlements)
  private val enemyZonesCache         = new Cache(() => zones.filter(_.owner.isEnemy))
  private val enemyBasesCache         = new Cache(() => bases.filter(_.owner.isEnemy))
  private val ourTownHallsCache       = new Cache(() => ourBases.flatMap(_.townHall))
  private val ourHarvestingAreasCache = new Cache(() => ourBases.map(_.harvestingArea))
  private val ourNaturalCache         = new Cache(() => bases.find(_.isNaturalOf.exists(_.owner.isUs)).getOrElse(bases.minBy(_.townHallTile.groundPixels(ourMain.townHallTile))))
  private val ourBorderCache          = new Cache(() => ourZones.flatMap(_.edges).filter(_.zones.exists( ! _.owner.isFriendly)))
  
  def zoneByTile(tile: Tile): Zone = zoneByTileCache(tile)
  private lazy val zoneByTileCache =
    new mutable.HashMap[Tile, Zone] {
      override def default(key: Tile): Zone = {
        val zone = zones
          .find(_.tiles.contains(key))
          .getOrElse(zones.minBy(_.centroid.tileDistanceSquared(key)))
        put(key, zone)
        zone
      }
    }
  
  private def getSettlements: Iterable[Base] = (Set.empty
  ++ With.geography.bases.toVector.filter(_.units.exists(u => u.isOurs && u.unitClass.isBuilding))
  ++ With.units.ours
    .filter(u => u.agent.toBuild.exists(_.isTownHall))
    .flatMap(u => u.agent.toBuildTile.map(tile => tile.zone.bases.find(base => base.townHallTile == tile)))
    .flatten
    .filterNot(_.owner.isUs)
  )
  
  var home: Tile = SpecificPoints.tileMiddle
  
  var naturalsSearched: Boolean = false
  
  def update() {
    ZoneUpdater.update()
    bases.filter(base => With.game.isVisible(base.townHallArea.midpoint.bwapi)).foreach(base => base.lastScoutedFrame = With.frame)
  }
}
