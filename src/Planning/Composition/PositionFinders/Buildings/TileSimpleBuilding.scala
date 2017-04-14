package Planning.Composition.PixelFinders.Buildings

import Planning.Composition.PixelFinders.TileFinder
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass.UnitClass
import Lifecycle.With
import Mathematics.Pixels.{Tile, TileRectangle}

class TileSimpleBuilding(val buildingClass:UnitClass) extends TileFinder {
  
  private var lastTile:Option[Tile] = None
  
  private var lastFailure = 0
  
  def find: Option[Tile] = {
    //Don't lag out due to lack of pylon space
    if (buildingClass.requiresPsi && ! With.units.ours.exists(u => u.complete && u.is(Protoss.Pylon))) {
      return None
    }
    
    if (With.frame < 24 * 60 * 3 || lastFailure < With.frame - 24 * 10) {
      lastTile = if (lastTileValid) lastTile else requestTile
      if (lastTile == None) {
        lastFailure = With.frame
      }
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
    if (buildingClass == Protoss.Pylon && With.units.ours.count(_.is(buildingClass)) < 3)
      4
    else
      1
  
  def exclusions:Iterable[TileRectangle] = {
    With.geography.bases.map(_.harvestingArea) ++
    With.realEstate.reserved
  }
  
  def requestTile:Option[Tile] = {
    
    // Performance optimization:
    // Don't waste time looking for a place to put a Gateway when we have no Pylons.
    if (buildingClass.requiresPsi && ! With.units.ours.filter(_.complete).exists(_.is(Protoss.Pylon))) {
      return None
    }
    
    var output:Option[Tile] = None
    
    (maxMargin to 1 by -1)
      .foreach(margin =>
        With.geography.ourBases
          .toVector
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
