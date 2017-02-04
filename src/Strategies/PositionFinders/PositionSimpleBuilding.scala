package Strategies.PositionFinders

import Caching.Cache
import Development.Logger
import Startup.With
import bwapi.{TilePosition, UnitType}

class PositionSimpleBuilding(
  val buildingType:UnitType)
    extends PositionFinder {
  
  val _cache = new Cache[Option[TilePosition]] { duration = 24 * 2; override def recalculate = _recalculate }
  override def find(): Option[TilePosition] = _cache.get
  
  def _recalculate: Option[TilePosition] = {
    val position = With.architect.getHq
    val output = With.architect.placeBuilding(
      buildingType,
      position,
      margin = 0,
      searchRadius = 50)
    
    if (output == None) {
      Logger.warn("Failed to place a " ++ buildingType.toString ++ " near " ++ position.toString)
    }
    
    output
  }
}
