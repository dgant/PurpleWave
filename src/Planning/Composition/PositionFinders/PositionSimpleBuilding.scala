package Planning.Composition.PositionFinders

import Geometry.TileRectangle
import Performance.Caching.Cache
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass.UnitClass
import Startup.With
import Utilities.EnrichPosition._
import bwapi.TilePosition

class PositionSimpleBuilding(val buildingType:UnitClass) extends PositionFinder {
  
  override def find: Option[TilePosition] = findCache.get
  private val findCache = new Cache[Option[TilePosition]](4, () => findRecalculate)
  private def findRecalculate: Option[TilePosition] = {
    
    //Short-circuits for performance
    if (buildingType.requiresPsi && ! With.units.ours.filter(_.complete).exists(_.unitClass == Protoss.Pylon)) {
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
    
    val candidates = With.geography.ourBases
      .flatten(_.gas)
      .filter(_.isNeutral)
      .map(_.tileTopLeft)
    
    if (candidates.isEmpty) None else Some(candidates.minBy(_.distanceTile(With.geography.home)))
  }
  
  private def positionTownHall:Option[TilePosition] = {
    val candidates = With.geography.bases
      .filterNot(_.zone.island)
      .filter(base => base.gasLeft > 0 || With.geography.ourBases.size >= 2)
      .map(base => base.townHallRectangle)
      .filter(baseRectangle => baseRectangle.tiles.forall(With.grids.buildable.get))
      .map(_.startInclusive)
  
    if (candidates.isEmpty) return None
    Some(candidates
      .minBy(candidate =>
        1 * With.geography.ourBases
          .map(base => With.paths.groundPixels(base.townHallRectangle.midpoint, candidate))
          .sum
        -
        2 * With.geography.enemyBases
          .map(base => With.paths.groundPixels(base.townHallRectangle.midpoint, candidate))
          .sum))
  }
  
  private def positionBuilding:Option[TilePosition] = {
    var output:Option[TilePosition] = None
    (maxMargin to 0 by -1).foreach(margin =>
      With.geography.ourBases
        .toList
        .sortBy( - _.mineralsLeft)
        .map(_.townHallRectangle.midpoint)
        .foreach(base =>
          output = output.orElse(
            With.architect.placeBuilding(
              buildingType,
              base,
              margin = margin,
              searchRadius = 20,
              exclusions = exclusions))))
    output
  }
  
  def maxMargin:Int = {
    if (buildingType.isRefinery)
      0
    else if (buildingType.isTownHall)
      0
    else if (buildingType == Protoss.Pylon && With.units.ours.count(_.unitClass == buildingType) < 6)
      3
    else
      1
  }
    
  def exclusions:Iterable[TileRectangle] = With.geography.bases.map(_.harvestingArea)
}
