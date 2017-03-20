package Information.Geography

import Geometry._
import Information.Geography.Types.{Base, Zone}
import Performance.Caching.Cache
import ProxyBwapi.UnitInfo.UnitInfo
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._
import bwapi.TilePosition

class Geography {
  
  private val baseCalculator = new Bases
  private val townHallPositionCalculator = new TownHallPositions
  
  def townHallPositions = townHallPositionCalculator.townHallPositions
  def zones:Iterable[Zone] = baseCalculator.zones
  
  def bases               : Iterable[Base]          = baseCalculator.zones.flatten(_.bases)
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
      .inRadius(townHallArea.midpoint.pixelCenter, 32 * 10)
      .filter(_.isResource)
      .map(_.tileArea)
    
    (List(townHallArea) ++ resources).boundary
  }
}
