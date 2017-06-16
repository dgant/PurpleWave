package Macro.SimCity

import Lifecycle.With
import Mathematics.Points.Tile
import Planning.Plan

class BuildingDescriptor(
  val requestor : Plan,
  val width     : Int,
  val height    : Int,
  val powers    : Boolean       = false,
  val powered   : Boolean       = false,
  val townHall  : Boolean       = false,
  val gas       : Boolean       = false,
  val margin    : Boolean       = false) {
  
  def fulfilledBy(suggestion: BuildingDescriptor): Boolean = {
    width     == suggestion.width                   &&
    height    == suggestion.height                  &&
    powers    == suggestion.powers    || ! powers   &&
    powered   == suggestion.powered   || ! powered  &&
    townHall  == suggestion.townHall                &&
    gas       == suggestion.gas                     &&
    margin    <= suggestion.margin
  }
  
  def accepts(tile: Tile): Boolean = {
    
    if ( ! tile.valid) return false
    
    if (powered) {
      if (width == 4 && ! With.grids.psi4x3.get(tile))        return false
      if (width == 3 && ! With.grids.psi2x2and3x2.get(tile))  return false
      if (width == 2 && ! With.grids.psi2x2and3x2.get(tile))  return false
    }
    
    if (townHall) {
      // TODO: Verify legality
    }
    
    if (gas) {
      // TODO: Verify legality
    }
    
    val marginTiles = if (margin) 1 else 0
    var x = tile.x - marginTiles
    var y = tile.y - marginTiles
    
    // TODO: Reject areas occupied by units
    
    // While loops have lower overhead than other iterative mechanisms in Scala.
    while (x < tile.x + width + marginTiles) {
      while (y < tile.y + height + marginTiles) {
        if (x < tile.x || y < tile.y || x > tile.x + width || y > tile.y + height) {
          if ( ! With.grids.walkable.get(Tile(x, y))) return false
        }
        else {
          if ( ! With.grids.buildable.get(Tile(x, y))) return false
        }
        y += 1
      }
      x += 1
    }
    
    true
  }
}