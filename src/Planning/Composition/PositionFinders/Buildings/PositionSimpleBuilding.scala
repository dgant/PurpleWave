package Planning.Composition.PositionFinders.Buildings

import Geometry.TileRectangle
import Planning.Composition.PositionFinders.PositionFinder
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass.UnitClass
import Startup.With
import bwapi.TilePosition

class PositionSimpleBuilding(val buildingClass:UnitClass) extends PositionFinder {
  
  private var lastTile:Option[TilePosition] = None
  
  def find: Option[TilePosition] = {
    lastTile = if (lastTileValid) lastTile else requestTile
    lastTile
  }
  
  def lastTileValid:Boolean =
    lastTile.exists(tile =>
      With.architect.canBuild(
        buildingClass,
        tile,
        maxMargin,
        exclusions))
  
  def maxMargin:Int =
    if (buildingClass.isRefinery)
      0
    else if (buildingClass.isTownHall)
      0
    else if (buildingClass == Protoss.Pylon && With.units.ours.count(_.unitClass == buildingClass) < 6)
      3
    else
      1
  
  def exclusions:Iterable[TileRectangle] = {
    With.geography.bases.map(_.harvestingArea) ++
    With.realEstate.reserved
  }
  
  def requestTile:Option[TilePosition] = {
    
    // Performance optimization:
    // Don't waste time looking for a place to put a Gateway when we have no Pylons.
    if (buildingClass.requiresPsi && ! With.units.ours.filter(_.complete).exists(_.unitClass == Protoss.Pylon)) {
      return None
    }
    
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
