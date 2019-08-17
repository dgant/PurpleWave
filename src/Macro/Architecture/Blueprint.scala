package Macro.Architecture

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.ArchitecturalAssessment.ArchitecturalAssessment
import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}
import Macro.Architecture.PlacementRequests.PlacementRequest
import Mathematics.Points.{Tile, TileRectangle}
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
  def accepts(tile: Tile, request: Option[PlacementRequest] = None): Boolean = {
    val reason = assess(tile, request)
    reason == ArchitecturalAssessment.Accepted
  }

  def assess(tile: Tile, request: Option[PlacementRequest] = None): ArchitecturalAssessment = {
    if ( ! tile.valid) {
      return ArchitecturalAssessment.Invalid
    }
    if ( ! matches(tile)) {
      return ArchitecturalAssessment.DoesntMatch
    }
    if (requirePower.get) {
      if (heightTiles.get == 3 && ! With.grids.psi3Height.isSet(tile) && ! With.architecture.powered3Height.get(tile)) {
        return ArchitecturalAssessment.Unpowered
      }
      if (heightTiles.get == 2 && ! With.grids.psi2Height.isSet(tile) && ! With.architecture.powered2Height.get(tile)) {
        return ArchitecturalAssessment.Unpowered
      }
    }

    val thisZone = tile.zone
    if (thisZone.island
      && ! Plasma.matches
      && ( ! With.blackboard.allowIslandBases() || ! thisZone.bases.exists(_.minerals.exists(_.visible)))
      && ! With.architecture.accessibleZones.contains(thisZone)) {
      return ArchitecturalAssessment.InaccessibleIsland
    }
    if (requireGasTile.get) {
      if ( ! thisZone.bases.exists(_.gas.exists(_.tileTopLeft == tile))) {
        return ArchitecturalAssessment.IsntGas
      }
      if ( ! With.architecture.gassable(tile, request)) {
        return ArchitecturalAssessment.BlockedGas
      }
      return ArchitecturalAssessment.Accepted
    }
    if (requireTownHallTile.get) {
      if ( ! thisZone.bases.exists(_.townHallTile == tile)) return ArchitecturalAssessment.IsntBasePosition
      if ( ! With.architecture.townhallable(tile, request)) return ArchitecturalAssessment.IsntLegalForTownHall
    }

    val buildArea = relativeBuildArea.add(tile)

    var x = buildArea.startInclusive.x
    val xMax = buildArea.endExclusive.x
    val yMax = buildArea.endExclusive.y
    while (x < xMax) {
      var y = buildArea.startInclusive.y
      while (y < yMax) {
        val nextTile = Tile(x, y)
        if ( ! With.architecture.buildable(nextTile, request)) {
          return ArchitecturalAssessment.IsntBuildable
        }
        if (nextTile.zone.perimeter.contains(nextTile)) {
          return ArchitecturalAssessment.ViolatesPerimeter
        }
        if (requireCreep.get != With.grids.creep.get(nextTile)) {
          return ArchitecturalAssessment.CreepMismatch
        }
        if (respectHarvesting.get && thisZone.bases.exists(_.harvestingArea.contains(nextTile))) {
          return ArchitecturalAssessment.ViolatesHarvesting
        }
        if (requireResourceGap.get && ! With.grids.buildableTownHall.get(nextTile)) {
          return ArchitecturalAssessment.ViolatesResourceGap
        }
        if ( ! requireTownHallTile.get && With.grids.units.get(nextTile).exists(u => ! u.flying && u.isEnemy || ! u.canMove)) {
          return ArchitecturalAssessment.BlockedByUnit
        }
        if (With.groundskeeper.isReserved(tile, request.flatMap(_.plan))) {
          return ArchitecturalAssessment.Reserved
        }
        y += 1
      }
      x += 1
    }
    ArchitecturalAssessment.Accepted
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