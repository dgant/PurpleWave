package Planning.Composition.PositionFinders.Buildings

import Performance.Caching.CacheFrame
import Planning.Composition.PositionFinders.PositionFinder
import ProxyBwapi.UnitClass.UnitClass
import bwapi.TilePosition

class PositionArbitraryBuilding(val buildingClass:UnitClass) extends PositionFinder {
  
  def find: Option[TilePosition] = positionCache.get
  private val positionCache = new CacheFrame(() => recalculate)
  
  val positionSimpleBuilding = new PositionSimpleBuilding(buildingClass)
  
  def recalculate: Option[TilePosition] = {
    if      (buildingClass.isRefinery)  return PositionRefinery.find
    else if (buildingClass.isTownHall)  return PositionTownHall.find
    else                                return positionSimpleBuilding.find
  }
}
