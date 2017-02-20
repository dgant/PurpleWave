package Strategies.PositionFinders

import Caching.Cache
import Startup.With
import bwapi.{TilePosition, UnitType}
import scala.collection.JavaConverters._

class PositionSimpleBuilding(
  val buildingType:UnitType)
    extends PositionFinder {
  
  val _cache = new Cache[Option[TilePosition]] { duration = 24 * 2; setCalculator(() => _recalculate) }
  override def find: Option[TilePosition] = _cache.get
  
  def _recalculate: Option[TilePosition] = {
    if (With.geography.ourBaseHalls.isEmpty) {
      With.logger.warn("Couldn't place a building because we had no town halls")
      return None
    }
    
    if (buildingType.isRefinery) {
      //cheap
      return Some(With.game.getStaticGeysers.asScala.minBy(_.getDistance(With.geography.ourBaseHalls.head)).getTilePosition)
    }
    
    
    val margin = if (buildingType == UnitType.Protoss_Pylon) 4 else 1
  
    val position = With.geography.ourBaseHalls.head.getTilePosition
    val output = With.architect.placeBuilding(
      buildingType,
      position,
      margin = margin,
      searchRadius = 50,
      exclusions = With.geography.ourHarvestingAreas)
    
    if (output == None) {
      With.logger.warn("Failed to place a " ++ buildingType.toString ++ " near " ++ position.toString)
    }
    
    output
  }
}
