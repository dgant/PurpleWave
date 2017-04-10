package Planning.Composition.PositionFinders.Buildings

import Planning.Composition.PositionFinders.TileFinder
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass.UnitClass
import Lifecycle.With
import Mathematics.Positions.TileRectangle
import bwapi.TilePosition

class TileSimpleBuilding(val buildingClass:UnitClass) extends TileFinder {
  
  private var lastTile:Option[TilePosition] = None
  
  private var lastFailure = 0
  
  def find: Option[TilePosition] = {
    if (With.frame > 24 * 60 * 5 || lastFailure < With.frame - 24 * 5) {
      lastTile = if (lastTileValid) lastTile else requestTile
    }
    
    if (lastTile == None) {
      lastFailure = With.frame
    }
    
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
    if (buildingClass == Protoss.Pylon && With.units.ours.count(_.unitClass == buildingClass) < 3)
      4
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
