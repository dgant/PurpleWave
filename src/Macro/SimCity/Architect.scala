package Macro.SimCity

import Lifecycle.With
import Mathematics.Points.{Tile, TileRectangle}
import Mathematics.Shapes.Spiral

class Architect {
  
  private val bases = With.geography.ourBases.toList.sortBy(base => - base.mineralsLeft * base.zone.area)
  
  def fulfill(buildingDescriptor: BuildingDescriptor, tile: Option[Tile]): Option[Tile] = {
   
    if (tile.isDefined && buildingDescriptor.accepts(tile.get)) {
      return tile
    }
    
    // TODO: Track exclusions
    
    
    None
  }
  
  def placeBuilding(
    buildingDescriptor  : BuildingDescriptor,
    exclusions          : Iterable[TileRectangle] = Vector.empty,
    searchRadius        : Int                     = 30)
      : Option[Tile] = {
    
    bases.flatten(base =>
      Spiral
        .points(searchRadius)
        .view
        .map(base.heart.add))
        .find(tile => buildingDescriptor.accepts(tile))
  }
}
