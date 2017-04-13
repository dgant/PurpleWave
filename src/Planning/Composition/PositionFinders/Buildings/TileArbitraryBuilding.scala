package Planning.Composition.PixelFinders.Buildings

import Mathematics.Pixels.Tile
import Performance.Caching.CacheFrame
import Planning.Composition.PixelFinders.TileFinder
import ProxyBwapi.UnitClass.UnitClass

class TileArbitraryBuilding(val buildingClass:UnitClass) extends TileFinder {
  
  def find: Option[Tile] = positionCache.get
  private val positionCache = new CacheFrame(() => recalculate)
  
  val tileSimpleBuilding = new TileSimpleBuilding(buildingClass)
  
  def recalculate: Option[Tile] = {
    if      (buildingClass.isRefinery)  return TileRefinery.find
    else if (buildingClass.isTownHall)  return TileTownHall.find
    else                                return tileSimpleBuilding.find
  }
}
