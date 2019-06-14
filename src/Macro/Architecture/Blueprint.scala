package Macro.Architecture

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}
import Mathematics.Points.{Tile, TileRectangle}
import Planning.Plan
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import Strategery.Plasma

class Blueprint(
  val building            : UnitClass,
  var widthTiles          : Option[Int]               = None,
  var heightTiles         : Option[Int]               = None,
  var powers              : Option[Boolean]           = None,
  var requirePower        : Option[Boolean]           = None,
  var requireCreep        : Option[Boolean]           = None,
  var requireTownHallTile : Option[Boolean]           = None,
  var requireGasTile      : Option[Boolean]           = None,
  var requireResourceGap  : Option[Boolean]           = None,
  var placement           : Option[PlacementProfile]  = None,
  var marginPixels        : Option[Double]            = None,
  val requireCandidates   : Option[Seq[Tile]]         = None,
  var preferZone          : Option[Zone]              = None,
  val requireZone         : Option[Zone]              = None,
  var respectHarvesting   : Option[Boolean]           = None,
  var forcePlacement      : Boolean                   = false) {
  
  var id: Option[Int] = None
  val frameCreated: Int = With.frame
  
  widthTiles                  = widthTiles                  .orElse(Some(building.tileWidth + (if (building.canBuildAddon && ! building.isTownHall) 2 else 0)))
  heightTiles                 = heightTiles                 .orElse(Some(building.tileHeight))
  powers                      = powers                      .orElse(Some(building == Protoss.Pylon))
  requirePower                = requirePower                .orElse(Some(building.requiresPsi))
  requireCreep                = requireCreep                .orElse(Some(building.requiresCreep))
  requireTownHallTile         = requireTownHallTile         .orElse(Some(building.isTownHall))
  requireGasTile              = requireGasTile              .orElse(Some(building.isRefinery))
  requireResourceGap          = requireResourceGap          .orElse(Some(building.isTownHall))
  preferZone                  = preferZone                  .orElse(requireZone)
  placement                   = placement                   .orElse(Some(PlacementProfiles.default(this)))
  respectHarvesting           = respectHarvesting           .orElse(Some( ! requireTownHallTile.get))
  marginPixels = marginPixels
    .orElse(Some(building).filter(_.attacks).map(_.effectiveRangePixels.toDouble))
    .orElse(Some(building).filter(_ == Protoss.ShieldBattery).map(b => 0.0))
    .orElse(Some(building).filter(_ == Zerg.CreepColony).map(b => 32.0 * 7.0))
    .orElse(Some(building).filter(_ == Protoss.RoboticsFacility).map(b => 32.0 * 5.0))
    .orElse(Some(32.0 * 11.0))
  
  def relativeBuildStart  : Tile  = Tile(0, 0)
  def relativeBuildEnd    : Tile  = Tile(widthTiles.get, heightTiles.get)
  def relativeBuildArea   : TileRectangle = TileRectangle(relativeBuildStart, relativeBuildEnd)
  
  protected def matches(tile: Tile): Boolean = {
    val thisZone = tile.zone
    if (requireZone.isDefined && ! requireZone.contains(thisZone)) {
      return false
    }
    true
  }
  
  protected def buildable(tile: Tile): Boolean = {
    if ( ! tile.valid) {
      return false
    }
    if (requirePower.get) {
      if (heightTiles.get == 3 && ! With.grids.psi3Height.isSet(tile) && ! With.architecture.powered3Height.get(tile)) {
        return false
      }
      if (heightTiles.get == 2 && ! With.grids.psi2Height.isSet(tile) && ! With.architecture.powered2Height.get(tile)) {
        return false
      }
    }
    
    true
  }
  
  def accepts(tile: Tile, plan: Option[Plan] = None): Boolean = {
    if ( ! matches(tile)) {
      return false
    }
    if ( ! buildable(tile)) {
      return false
    }

    val thisZone = tile.zone
    if (thisZone.island
      && ! Plasma.matches
      && ( ! With.blackboard.allowIslandBases() || ! thisZone.bases.exists(_.minerals.exists(_.visible)))
      && ! With.architecture.accessibleZones.contains(thisZone)) {
      return false
    }
    if (requireGasTile.get) {
      val legal   = thisZone.bases.exists(_.gas.exists(_.tileTopLeft == tile))
      val blocked = With.architecture.ungassable.get(tile)
      return legal && ! blocked
    }
    if (requireTownHallTile.get) {
      if ( ! thisZone.bases.exists(_.townHallTile == tile)) return false
      if (With.architecture.untownhallable.get(tile)) return false
    }

    val buildArea = relativeBuildArea.add(tile)
  
    def violatesBuildArea(nextTile: Tile): Boolean = (
      nextTile.zone.perimeter.contains(nextTile)
      || ! With.architecture.buildable(nextTile)
      || (requireCreep.get != With.grids.creep.get(nextTile))
      || (respectHarvesting.get && With.grids.harvestingArea.get((nextTile)))
      || (requireResourceGap.get && ! With.grids.buildableTownHall.get(nextTile))
      || ( ! requireTownHallTile.get && With.grids.units.get(nextTile).exists(u => ! u.flying && u.isEnemy || ! u.canMove))
      || (With.groundskeeper.isReserved(tile, nextTile, plan))
    )

    var x = buildArea.startInclusive.x
    val xMax = buildArea.endExclusive.x
    val yMax = buildArea.endExclusive.y
    while (x < xMax) {
      var y = buildArea.startInclusive.y
      while (y < yMax) {
        if (violatesBuildArea(Tile(x, y))) {
          return false
        }
        y += 1
      }
      x += 1
    }
    true
  }
  
  override def toString: String =
    (
      id.map(_.toString).getOrElse("x") + " " +
      building.toString + " " +
      placement.toString + " " +
      widthTiles + "x" + heightTiles + " " +
      (if (powers.get)              "(Powers) "     else "") +
      (if (requirePower.get)        "(Powered) "    else "") +
      (if (requireTownHallTile.get) "(Town hall) "  else "") +
      (if (requireGasTile.get)      "(Gas) "        else "")
    ).replaceAllLiterally("Some", "")
}