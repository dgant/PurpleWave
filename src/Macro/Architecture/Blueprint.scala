package Macro.Architecture

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}
import Mathematics.Points.{Tile, TileRectangle}
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
  
  var id: Option[Int] = None
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
    width         == proposal.width                                                               &&
    height        == proposal.height                                                              &&
    (powers       == proposal.powers          || ! powers)                                        &&
    (powered      == proposal.powered         || ! powered)                                       &&
    townHall      == proposal.townHall                                                            &&
    gas           == proposal.gas                                                                 &&
    // Disabled to allow FFE to work
    //(margin       <= proposal.margin          || With.configuration.enableTightBuildingPlacement) &&
    (zone         == proposal.zone            || zone.isEmpty)                                    &&
    (building     == proposal.building        || building.isEmpty || proposal.building.isEmpty)
  }
  
  def marginTiles         : Int   = if(margin) 1 else 0
  def relativeBuildStart  : Tile  = Tile(0, 0)
  def relativeBuildEnd    : Tile  = Tile(width, height)  
  def relativeMarginStart : Tile  = relativeBuildStart.subtract(marginTiles, marginTiles)
  def relativeMarginEnd   : Tile  = relativeBuildEnd.add(marginTiles, marginTiles)
  def relativeBuildArea   : TileRectangle = TileRectangle(relativeBuildStart, relativeBuildEnd)
  def relativeMarginArea  : TileRectangle = TileRectangle(relativeMarginStart, relativeMarginEnd)
  
  def accepts(tile: Tile): Boolean = {
    
    if ( ! tile.valid) {
      return false
    }
    
    if (powered) {
      if (height == 3 && ! With.grids.psi3Height.get(tile) && ! With.architecture.powered3Height.contains(tile)) {
        return false
      }
      if (height == 2 && ! With.grids.psi2Height.get(tile) && ! With.architecture.powered2Height.contains(tile)) {
        return false
      }
    }
    
    if (tile.zone.island) {
      return false
    }
    
    if (zone.exists(_ != tile.zone)) {
      return false
    }
  
    if (townHall) {
      return ! With.architecture.untownhallable.contains(tile)
    }
  
    if (gas) {
      return ! With.architecture.ungassable.contains(tile)
    }
    
    val marginArea      = relativeMarginArea.add(tile)
    lazy val buildArea  = relativeBuildArea.add(tile)
  
    marginArea.tiles.forall(nextTile => {
      nextTile.valid &&
      (
        if (buildArea.contains(nextTile)) {
          With.architecture.buildable(nextTile)
        }
        else {
          With.architecture.walkable(nextTile)
        }
      )
    })
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