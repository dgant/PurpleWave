package Planning.Composition.PositionFinders

import Geometry.TileRectangle
import Performance.Caching.Cache
import ProxyBwapi.UnitClass.{Protoss, UnitClass}
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._
import bwapi.{TilePosition, UnitType}

class PositionSimpleBuilding(
  val buildingType:UnitClass)
    extends PositionFinder {
  
  override def find: Option[TilePosition] = findCache.get
  private val findCache = new Cache[Option[TilePosition]](2, () => findRecalculate)
  private def findRecalculate: Option[TilePosition] = {
    
    //Short-circuits for performance
    if (buildingType.requiresPsi && ! With.units.ours.exists(_.utype == UnitType.Protoss_Pylon)) {
      return None
    }
    if (findCache.lastValue.isDefined && findCache.lastValue.get.isDefined) {
      val lastPosition = findCache.lastValue.get.get
      
      if (With.architect.canBuild(buildingType, lastPosition, maxMargin, exclusions)) {
        return Some(lastPosition)
      }
    }
    
    if (buildingType.isRefinery)      return positionRefinery
    else if (buildingType.isTownHall) return positionTownHall
    else                              return positionBuilding
  }
  
  private def positionRefinery:Option[TilePosition] = {
    
    val candidates = With.units.neutral
      .filter(_.isGas)
      .filter(gas =>
        With.geography.bases.exists(base =>
          base.zone.owner == With.self &&
          base.harvestingArea.contains(gas.tileCenter)))
      .map(_.tileTopLeft)
    
    if (candidates.isEmpty) None else Some(candidates.minBy(_.tileDistance(With.geography.home)))
  }
  
  private def positionTownHall:Option[TilePosition] = {
    val candidates = With.geography.townHallPositions
      .filter(basePosition => {
        val rectangle = new TileRectangle(basePosition, basePosition.add(buildingType.tileSize))
        With.units.all.filter(_.utype.isBuilding).forall( ! _.tileArea.intersects(rectangle))
      })
  
    if (candidates.isEmpty) None else Some(candidates.minBy(With.paths.groundDistance(_, With.geography.home)))
  }
  
  private def positionBuilding:Option[TilePosition] = {
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
    else if (buildingType == Protoss.Pylon && With.units.ours.count(_.utype == buildingType) < 4)
      3
    else
      1
  }
    
  def exclusions:Iterable[TileRectangle] = With.geography.bases.map(_.harvestingArea)
}
