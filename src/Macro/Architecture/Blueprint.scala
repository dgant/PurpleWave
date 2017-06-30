package Macro.Architecture

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}
import Mathematics.Points.Tile
import Planning.Plan
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass.UnitClass

class Blueprint(
  val proposer    : Plan,
  val building    : Option[UnitClass]         = None,
  argWidth        : Option[Int]               = None,
  argHeight       : Option[Int]               = None,
  argPowers       : Option[Boolean]           = None,
  argPowered      : Option[Boolean]           = None,
  argTownHall     : Option[Boolean]           = None,
  argGas          : Option[Boolean]           = None,
  argMargin       : Option[Boolean]           = None,
  argPlacement    : Option[PlacementProfile]  = None,
  argRangePixels  : Option[Double]            = None,
  val zone        : Option[Zone]              = None) {
  
  val frameCreated: Int = With.frame
  
  val width       : Int               = argWidth        .orElse(building.map(_.tileWidth)).getOrElse(1)
  val height      : Int               = argHeight       .orElse(building.map(_.tileHeight)).getOrElse(1)
  val powers      : Boolean           = argPowers       .getOrElse(building.contains(Protoss.Pylon))
  val powered     : Boolean           = argPowered      .getOrElse(building.exists(_.requiresPsi))
  val townHall    : Boolean           = argTownHall     .getOrElse(building.exists(_.isTownHall))
  val gas         : Boolean           = argGas          .getOrElse(building.exists(_.isRefinery))
  val margin      : Boolean           = argMargin       .getOrElse(building.exists(With.architecture.usuallyNeedsMargin))
  val attackRange : Option[Double]    = argRangePixels  .orElse(building.map(building => building.maxAirGroundRange + building.radialHypotenuse))
  val placement   : PlacementProfile  = argPlacement    .getOrElse(PlacementProfiles.default(this))
  
  def fulfilledBy(proposal: Blueprint): Boolean = {
    if (proposal == this) return true
    width         == proposal.width                             &&
    height        == proposal.height                            &&
    (powers       == proposal.powers          || ! powers)      &&
    (powered      == proposal.powered         || ! powered)     &&
    townHall      == proposal.townHall                          &&
    gas           == proposal.gas                               &&
    margin        <= proposal.margin                            &&
    (zone         == proposal.zone            || zone.isEmpty)  &&
    (building     == proposal.building        || building.isEmpty || proposal.building.isEmpty)
  }
  
  def marginTiles         : Int   = if(margin) 1 else 0
  def relativeBuildStart  : Tile  = Tile(0, 0)
  def relativeBuildEnd    : Tile  = Tile(width, height)
  def relativeMarginStart : Tile  = relativeBuildStart.subtract(marginTiles, marginTiles)
  def relativeMarginEnd   : Tile  = relativeBuildEnd.add(marginTiles, marginTiles)
  
  def accepts(tile: Tile): Boolean = {
    
    if (powered) {
      if (width == 4 && ! With.grids.psi3Height.get(tile) && ! With.architecture.powered3Height.contains(tile)) {
        return false
      }
      if (width == 3 && ! With.grids.psi2Height.get(tile) && ! With.architecture.powered2Height.contains(tile)) {
        return false
      }
      if (width == 2 && ! With.grids.psi2Height.get(tile) && ! With.architecture.powered2Height.contains(tile)) {
        return false
      }
    }
    
    if (townHall) {
      if ( ! With.geography.bases.exists(base => base.townHallArea.startInclusive == tile)) {
        return false
      }
    }
    
    if (gas) {
      if( ! With.units.neutral.exists(unit => unit.unitClass.isGas && unit.tileTopLeft == tile)) {
        return false
      }
    }
    
    if (tile.zone.island) {
      return false
    }
    
    if (zone.exists(_ != tile.zone)) {
      return false
    }
    
    var x             = tile.add(relativeMarginStart).x
    val xMax          = tile.add(relativeMarginEnd).x
    val yMax          = tile.add(relativeMarginEnd).y
    val tileBuildEnd  = tile.add(relativeBuildEnd)
    
    // While loops have lower overhead than other iterative mechanisms in Scala.
    while (x < xMax) {
      var y = tile.add(relativeMarginStart).y
      while (y < yMax) {
        val nextTile = Tile(x, y)
        if ( ! nextTile.valid) {
          return false
        }
        if (
          nextTile.x < tile.x           ||
          nextTile.y < tile.y           ||
          nextTile.x >= tileBuildEnd.x  ||
          nextTile.y >= tileBuildEnd.y) {
          if ( ! With.architecture.walkable(tile)) {
            return false
          }
        }
        else if ( ! gas && ! With.grids.buildable.get(nextTile)) {
            return false
        }
        y += 1
      }
      x += 1
    }
    
    true
  }
  
  override def toString: String =
    "#" + proposer.priority + " " +
    proposer.toString.take(12) + " " +
    building.map(_.toString + " ").getOrElse("") +
    placement.toString + " " +
    width + "x" + height + " " +
    (if (margin) (width + 2 * marginTiles) + "x" + (height + 2 * marginTiles) + " " else "") +
    (if (powers)    "(Powers) "     else "") +
    (if (powered)   "(Powered) "    else "") +
    (if (townHall)  "(Town hall) "  else "") +
    (if (gas)       "(Gas) "        else "")
  
}