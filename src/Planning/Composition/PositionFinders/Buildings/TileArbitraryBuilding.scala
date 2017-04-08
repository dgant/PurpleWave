package Planning.Composition.PositionFinders.Buildings

import Performance.Caching.CacheFrame
import Planning.Composition.PositionFinders.TileFinder
import ProxyBwapi.UnitClass.UnitClass
import bwapi.TilePosition

class TileArbitraryBuilding(val buildingClass:UnitClass) extends TileFinder {
  
  def find: Option[TilePosition] = positionCache.get
  private val positionCache = new CacheFrame(() => recalculate)
  
  val positionSimpleBuilding = new TileSimpleBuilding(buildingClass)
  
  def recalculate: Option[TilePosition] = {
    if      (buildingClass.isRefinery)  return TileRefinery$.find
    else if (buildingClass.isTownHall)  return TileTownHall$.find
    else                                return positionSimpleBuilding.find
  }
}
