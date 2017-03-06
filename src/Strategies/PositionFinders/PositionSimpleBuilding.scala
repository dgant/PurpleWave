package Strategies.PositionFinders

import Geometry.TileRectangle
import Startup.With
import Utilities.Caching.Cache
import Utilities.Enrichment.EnrichUnitType._
import Utilities.Enrichment.EnrichPosition._
import bwapi.{TilePosition, UnitType}

class PositionSimpleBuilding(
  val buildingType:UnitType)
    extends PositionFinder {
  
  val _cache = new Cache[Option[TilePosition]](24 * 2, () => _find)
  
  override def find: Option[TilePosition] = _cache.get
  
  def _find: Option[TilePosition] = {
    val home = With.geography.home
    
    if (buildingType.isRefinery) {
      val candidates = With.units.neutral.filter(_.isGas)
      return if (candidates.isEmpty) None else Some(candidates.minBy(_.tileTopLeft.getDistance(home)).tileTopLeft)
    }
    else if (buildingType.isTownHall) {
      val candidates = With.geography.townHallPositions.filter(basePosition => {
        val rectangle = new TileRectangle(basePosition, basePosition.add(buildingType.tileSize))
        With.units.all.filter(_.utype.isBuilding).forall( ! _.tileArea.intersects(rectangle))
      })
        
      return if (candidates.isEmpty) None else Some(candidates.minBy(With.paths.groundDistance(_, home)))
    }
    
    val maxMargin = if (
      buildingType == UnitType.Protoss_Pylon &&
      With.units.ours.filter(_.utype == buildingType).size < 4) 3 else 1
  
    var output:Option[TilePosition] = None
    (0 to maxMargin).reverse.foreach(margin =>
      output = output.orElse(
        With.architect.placeBuilding(
        buildingType,
        home,
        margin = margin,
        searchRadius = 40,
        exclusions = With.geography.ourHarvestingAreas)))
    
    if (output == None) {
      With.logger.warn("Failed to place a " ++ buildingType.toString ++ " near " ++ home.toString)
    }
    
    output
  }
}
