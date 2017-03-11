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
    
    if (buildingType.isRefinery) return _positionRefinery
    else if (buildingType.isTownHall) return _positionTownHall
    
    val maxMargin = if (buildingType == UnitType.Protoss_Pylon) 3 else 1
  
    var output:Option[TilePosition] = None
    (maxMargin to 0 by -1).foreach(margin =>
      output = output.orElse(
        With.architect.placeBuilding(
        buildingType,
        home,
        margin = margin,
        searchRadius = 50,
        exclusions = With.geography.bases.map(_.harvestingArea))))
    
    output
  }
  
  def _positionRefinery:Option[TilePosition] = {
    
    val candidates = With.units.neutral
      .filter(_.isGas)
      .filter(gas =>
        With.geography.bases.exists(base =>
          base.zone.owner == With.game.self &&
          base.harvestingArea.contains(gas.tileCenter)))
    
    return if (candidates.isEmpty)
      None
    else
      Some(candidates.minBy(_.tileTopLeft.getDistance(With.geography.home)).tileTopLeft)
  }
  
  def _positionTownHall:Option[TilePosition] = {
    val candidates = With.geography.townHallPositions.filter(basePosition => {
      val rectangle = new TileRectangle(basePosition, basePosition.add(buildingType.tileSize))
      With.units.all.filter(_.utype.isBuilding).forall( ! _.tileArea.intersects(rectangle))
    })
  
    return if (candidates.isEmpty)
      None
    else Some(candidates.minBy(With.paths.groundDistance(_, With.geography.home)))
  }
}
