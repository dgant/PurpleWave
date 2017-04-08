package Planning.Composition.PositionFinders.Buildings

import Planning.Composition.PositionFinders.PositionFinder
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass.UnitClass
import Lifecycle.With
import Mathematics.Positions.TileRectangle
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
    if (buildingClass == Protoss.Pylon && With.units.ours.count(_.unitClass == buildingClass) < 4)
      1
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
    
    (maxMargin to 1 by -1)
      .foreach(margin =>
        With.geography.ourBases
          .toList
          .sortBy(base => - base.mineralsLeft * base.zone.area)
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
