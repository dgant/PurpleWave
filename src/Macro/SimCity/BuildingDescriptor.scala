package Macro.SimCity

import Lifecycle.With
import Mathematics.Points.Tile
import Planning.Plan
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass.{UnitClass, UnitClasses}

class BuildingDescriptor(
  val requestor : Plan,
  val width     : Int,
  val height    : Int,
  val powers    : Boolean       = false,
  val powered   : Boolean       = false,
  val townHall  : Boolean       = false,
  val gas       : Boolean       = false,
  val margin    : Boolean       = false) {
  
  def this(
    requestor : Plan,
    building  : UnitClass,
    width     : Option[Int]     = None,
    height    : Option[Int]     = None,
    powers    : Option[Boolean] = None,
    powered   : Option[Boolean] = None,
    townHall  : Option[Boolean] = None,
    gas       : Option[Boolean] = None,
    margin    : Option[Boolean] = None) {
    this(
      requestor,
      width     = width     .getOrElse(building.tileWidth),
      height    = height    .getOrElse(building.tileHeight),
      powers    = powers    .getOrElse(building == Protoss.Pylon),
      powered   = powered   .getOrElse(building.requiresPsi),
      townHall  = townHall  .getOrElse(building.isTownHall),
      gas       = gas       .getOrElse(building.isRefinery),
      margin    = margin    .getOrElse(UnitClasses.all.exists(unit => ! unit.isFlyer && unit.whatBuilds._1 == building))
    )
  }
  
  def fulfilledBy(suggestion: BuildingDescriptor): Boolean = {
    width     == suggestion.width                   &&
    height    == suggestion.height                  &&
    powers    == suggestion.powers    || ! powers   &&
    powered   == suggestion.powered   || ! powered  &&
    townHall  == suggestion.townHall                &&
    gas       == suggestion.gas                     &&
    margin    <= suggestion.margin
  }
  
  def marginTiles: Int = if(margin) 1 else 0
  
  def buildStart  : Tile = Tile(0, 0)
  def buildEnd    : Tile = Tile(width, height)
  def marginStart : Tile = buildStart.subtract(marginTiles, marginTiles)
  def marginEnd   : Tile = buildEnd.add(marginTiles, marginTiles)
  
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
    
    var x             = tile.add(marginStart).x
    var y             = tile.add(marginStart).y
    val xMax          = tile.add(marginEnd).x
    val yMax          = tile.add(marginEnd).y
    val tileBuildEnd  = tile.add(buildEnd)
    
    // TODO: Reject areas occupied by units
    
    // While loops have lower overhead than other iterative mechanisms in Scala.
    while (x < xMax) {
      while (y < yMax) {
        val nextTile = Tile(x, y)
        if (
          nextTile.x < tile.x         ||
          nextTile.y < tile.y         ||
          nextTile.x > tileBuildEnd.x ||
          nextTile.y > tileBuildEnd.y) {
          if ( ! With.grids.walkable.get(nextTile)) return false
        }
        else {
          if ( ! With.grids.buildable.get(nextTile)) return false
        }
        y += 1
      }
      x += 1
    }
    
    true
  }
}