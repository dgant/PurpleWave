package Planning.Composition.PositionFinders.Buildings

import Geometry.TileRectangle
import Performance.Caching.Cache
import Planning.Composition.PositionFinders.PositionFinder
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass.UnitClass
import Startup.With
import bwapi.TilePosition

class PositionSimpleBuilding(val buildingClass:UnitClass) extends PositionFinder {
  
  def find: Option[TilePosition] = findCache.get
  
  private val findCache = new Cache(1, () => recalculate)
  
  def recalculate: Option[TilePosition] = {
    
    //Short circuit for performance
    if (buildingClass.requiresPsi && ! With.units.ours.filter(_.complete).exists(_.unitClass == Protoss.Pylon)) {
      return None
    }
  
    if (stillValid) findCache.get else requestTile
  }
  
  def stillValid:Boolean = {
    if (findCache.lastValue.isEmpty || findCache.lastValue.get.isEmpty) return false
    val tile = findCache.lastValue.get.get
    With.architect.canBuild(
      buildingClass,
      tile,
      maxMargin,
      exclusions)
  }
  
  def maxMargin:Int = {
    if (buildingClass.isRefinery)
      0
    else if (buildingClass.isTownHall)
      0
    else if (buildingClass == Protoss.Pylon && With.units.ours.count(_.unitClass == buildingClass) < 6)
      3
    else
      1
  }
  
  def exclusions:Iterable[TileRectangle] = {
    With.geography.bases.map(_.harvestingArea) ++
    With.reservations.requestedAreas.values
  }
  
  def requestTile:Option[TilePosition] = {
    
    var output:Option[TilePosition] = None
    
    (maxMargin to 0 by -1)
      .foreach(margin =>
        With.geography.ourBases
          .toList
          .sortBy( - _.mineralsLeft)
          .map(_.townHallArea.midpoint)
          .foreach(tile =>
            output = output.orElse(
              With.architect.placeBuilding(
                buildingClass,
                tile,
                margin = margin,
                searchRadius = 25,
                exclusions = exclusions))))
    output
  }
}
