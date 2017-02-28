package Strategies.PositionFinders

import Utilities.Cache
import Startup.With
import bwapi.{TilePosition, UnitType}
import scala.collection.JavaConverters._

class PositionSimpleBuilding(
  val buildingType:UnitType)
    extends PositionFinder {
  
  val _cache = new Cache[Option[TilePosition]] { duration = 24 * 2; setCalculator(() => _recalculate) }
  override def find: Option[TilePosition] = _cache.get
  
  def _recalculate: Option[TilePosition] = {
    val startPosition = With.geography.home.toTilePosition
    
    if (buildingType.isRefinery) {
      //cheap
      return Some(With.game.getStaticGeysers.asScala.minBy(_.getTilePosition.getDistance(startPosition)).getTilePosition)
    }
    
    val margin = if (buildingType == UnitType.Protoss_Pylon) 4 else 1
  
    val output = With.architect.placeBuilding(
      buildingType,
      startPosition,
      margin = margin,
      searchRadius = 50,
      exclusions = With.geography.ourHarvestingAreas)
    
    if (output == None) {
      With.logger.warn("Failed to place a " ++ buildingType.toString ++ " near " ++ startPosition.toString)
    }
    
    output
  }
}
