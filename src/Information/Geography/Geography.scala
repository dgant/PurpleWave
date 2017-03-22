package Information.Geography

import Geometry._
import Information.Geography.Types.{Base, Zone}
import Performance.Caching.{Cache, CacheForever}
import ProxyBwapi.UnitInfo.UnitInfo
import Startup.With
import Utilities.EnrichPosition._
import bwapi.TilePosition

class Geography {
  
  private val baseCalculator = new Bases
  
  val townHallPositionCache = new CacheForever(() => TownHallPositionCalculator.calculate)
  def townHallPositions = townHallPositionCache.get
  def zones:Iterable[Zone] = baseCalculator.zones
  
  def bases               : Iterable[Base]          = zones.flatten(_.bases)
  def ourBases            : Iterable[Base]          = bases.filter(_.zone.owner == With.self)
  def ourBaseHalls        : Iterable[UnitInfo]      = ourBases.filter(_.townHall.isDefined).map(_.townHall.get)
  def ourHarvestingAreas  : Iterable[TileRectangle] = ourBases.map(_.harvestingArea)
  
  def home:TilePosition = homeCache.get
  private val homeCache = new Cache[TilePosition](5, () => homeCalculate)
  private def homeCalculate:TilePosition =
    ourBases.toList
      .sortBy( ! _.isStartLocation).map(_.centerTile)
      .headOption
      .getOrElse(With.units.ours.view.filter(_.unitClass.isBuilding).map(_.tileCenter).headOption
      .getOrElse(Positions.tileMiddle))
  
  def getHarvestingArea(townHallArea:TileRectangle):TileRectangle = {
    val resources = With.units
      .inPixelRadius(townHallArea.midpoint.pixelCenter, 32 * 10)
      .filter(_.unitClass.isResource)
      .map(_.tileArea)
    
    (List(townHallArea) ++ resources).boundary
  }
}
