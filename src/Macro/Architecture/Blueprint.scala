package Macro.Architecture

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}
import Mathematics.Points.{Tile, TileRectangle}
import Planning.Plan
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass.UnitClass

class Blueprint(
val proposer                   : Plan,
val building                   : Option[UnitClass]         = None,
var widthTiles                 : Option[Int]               = None,
var heightTiles                : Option[Int]               = None,
var powers                     : Option[Boolean]           = None,
var requirePower               : Option[Boolean]           = None,
var requireCreep               : Option[Boolean]           = None,
var requireTownHallTile        : Option[Boolean]           = None,
var requireGasTile             : Option[Boolean]           = None,
var placementProfile           : Option[PlacementProfile]  = None,
var preferredDistanceFromExit  : Option[Double]            = None,
val requireCandidates          : Option[Iterable[Tile]]    = None,
var preferZone                 : Option[Zone]              = None,
val requireZone                : Option[Zone]              = None,
var respectHarvesting          : Boolean                   = true) {
  
  var id: Option[Int] = None
  val frameCreated: Int = With.frame
  
  widthTiles                  = widthTiles                  .orElse(building.map(b => b.tileWidth + (if (b.canBuildAddon) 2 else 0))).orElse(Some(1))
  heightTiles                 = heightTiles                 .orElse(building.map(_.tileHeight)).orElse(Some(1))
  powers                      = powers                      .orElse(Some(building.contains(Protoss.Pylon)))
  requirePower                = requirePower                .orElse(Some(building.exists(_.requiresPsi)))
  requireCreep                = requireCreep                .orElse(Some(building.exists(_.requiresCreep)))
  requireTownHallTile         = requireTownHallTile         .orElse(Some(building.exists(_.isTownHall)))
  requireGasTile              = requireGasTile              .orElse(Some(building.exists(_.isRefinery)))
  preferredDistanceFromExit   = preferredDistanceFromExit   .orElse(if (building.exists(_.attacks)) building.map(_.effectiveRangePixels.toDouble) else Some(32.0 * 9.0))
  preferZone                  = preferZone                  .orElse(requireZone)
  placementProfile            = placementProfile            .orElse(Some(PlacementProfiles.default(this)))
  
  def fulfilledBy(proposal: Blueprint): Boolean = {
    if (proposal == this) return true
    widthTiles.get  <= proposal.widthTiles.get                        &&
    heightTiles.get <= proposal.heightTiles.get                       &&
    proposal.powers == powers                                         &&
    ( ! requirePower.get        || proposal.requirePower.get)         &&
    ( ! requireTownHallTile.get || proposal.requireTownHallTile.get)  &&
    requireGasTile.get == proposal.requireGasTile.get                 &&
    (requireZone.isEmpty || requireZone == proposal.requireZone)      &&
    (proposal.building.isEmpty || building == proposal.building)
  }
  
  def relativeBuildStart  : Tile  = Tile(0, 0)
  def relativeBuildEnd    : Tile  = Tile(widthTiles.get, heightTiles.get)
  def relativeBuildArea   : TileRectangle = TileRectangle(relativeBuildStart, relativeBuildEnd)
  
  def accepts(tile: Tile): Boolean = {
    
    if ( ! tile.valid) {
      return false
    }
    
    if (requirePower.get) {
      if (heightTiles.get == 3 && ! With.grids.psi3Height.get(tile) && ! With.architecture.powered3Height.contains(tile)) {
        return false
      }
      if (heightTiles.get == 2 && ! With.grids.psi2Height.get(tile) && ! With.architecture.powered2Height.contains(tile)) {
        return false
      }
    }
    
    val thisZone = tile.zone
  
  
    if (thisZone.island && ! With.strategy.isPlasma) {
      return false
    }
    
    if (requireZone.isDefined && ! requireZone.contains(thisZone)) {
      return false
    }
  
    if (requireTownHallTile.get) {
      val legal   = thisZone.bases.exists(_.townHallTile == tile)
      val blocked = With.architecture.untownhallable.contains(tile)
      return legal && ! blocked
    }
  
    if (requireGasTile.get) {
      val legal   = thisZone.bases.exists(_.gas.exists(_.tileTopLeft == tile))
      val blocked = With.architecture.ungassable.contains(tile)
      return legal && ! blocked
    }
    
    val buildArea = relativeBuildArea.add(tile)
  
    def violatesBuildArea(nextTile: Tile): Boolean = {
      nextTile.zone.perimeter.contains(nextTile)                ||
      ! With.architecture.buildable(nextTile)                   ||
      (requireCreep.get   && ! With.grids.creep.get(nextTile))  ||
      (respectHarvesting  && With.architecture.isHarvestingArea(nextTile))
    }
    
    val violator = buildArea.tiles.find(nextTile => violatesBuildArea(nextTile))
    
    violator.isEmpty
  }
  
  override def toString: String =
    "#" + proposer.priority +    " " +
    building.map(_.toString + " ").getOrElse("") +
    placementProfile.toString + " " +
    widthTiles + "x" + heightTiles + " " +
    (if (powers.get)              "(Powers) "     else "") +
    (if (requirePower.get)        "(Powered) "    else "") +
    (if (requireTownHallTile.get) "(Town hall) "  else "") +
    (if (requireGasTile.get)      "(Gas) "        else "")
}