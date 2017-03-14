package Strategies.PositionFinders

import Geometry.TileRectangle
import Startup.With
import Utilities.Caching.Cache
import Utilities.Enrichment.EnrichPosition._
import Utilities.Enrichment.EnrichUnitType._
import bwapi.{TilePosition, UnitType}

class PositionSimpleBuilding(
  val buildingType:UnitType)
    extends PositionFinder {
  
  val _cache = new Cache[Option[TilePosition]](2, () => _find)
  
  override def find: Option[TilePosition] = _cache.get
  
  def _find: Option[TilePosition] = {
    
    if (_cache.lastValue.isDefined && _cache.lastValue.get.isDefined) {
      val lastPosition = _cache.lastValue.get.get
      
      if (With.architect.canBuild(buildingType, lastPosition, maxMargin, exclusions)) {
        return Some(lastPosition)
      }
    }
    
    if (buildingType.isRefinery)      return _positionRefinery
    else if (buildingType.isTownHall) return _positionTownHall
    else                              return _positionBuilding
  }
  
  def _positionRefinery:Option[TilePosition] = {
    
    val candidates = With.units.neutral
      .filter(_.isGas)
      .filter(gas =>
        With.geography.bases.exists(base =>
          base.zone.owner == With.game.self &&
          base.harvestingArea.contains(gas.tileCenter)))
      .map(_.tileTopLeft)
    
    if (candidates.isEmpty) None else Some(candidates.minBy(_.tileDistance(With.geography.home)))
  }
  
  def _positionTownHall:Option[TilePosition] = {
    val candidates = With.geography.townHallPositions
      .filter(basePosition => {
        val rectangle = new TileRectangle(basePosition, basePosition.add(buildingType.tileSize))
        With.units.all.filter(_.utype.isBuilding).forall( ! _.tileArea.intersects(rectangle))
      })
  
    if (candidates.isEmpty) None else Some(candidates.minBy(With.paths.groundDistance(_, With.geography.home)))
  }
  
  def _positionBuilding:Option[TilePosition] = {
    var output:Option[TilePosition] = None
    (maxMargin to 0 by -1).foreach(margin =>
      output = output.orElse(
        With.architect.placeBuilding(
          buildingType,
          With.geography.home,
          margin = margin,
          searchRadius = 50,
          exclusions = exclusions)))
    output
  }
  
  def maxMargin:Int = {
    if (buildingType.isRefinery)
      0
    else if (buildingType.isTownHall)
      0
    else if (buildingType == UnitType.Protoss_Pylon && With.units.ours.count(_.utype == buildingType) < 4)
      3
    else
      1
  }
    
  def exclusions:Iterable[TileRectangle] = With.geography.bases.map(_.harvestingArea)
}
