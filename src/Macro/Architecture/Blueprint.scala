package Macro.Architecture

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.ArchitecturalAssessment.ArchitecturalAssessment
import Mathematics.Points.{Tile, TileRectangle}
import ProxyBwapi.UnitClasses.UnitClass
import Strategery.Plasma

class Blueprint(
  val building          : UnitClass,
  val requireCandidates : Option[Seq[Tile]] = None,
  val requireZone       : Option[Zone]      = None) {

  val width: Int = building.tileWidth + (if (building.canBuildAddon && ! building.isTownHall) 2 else 0)
  val height: Int = building.tileHeight
  
  protected def matches(tile: Tile): Boolean = requireZone.forall(tile.zone==)

  def accepts(tile: Tile, request: Option[PlacedBlueprint] = None): Boolean = {
    val reason = assess(tile, request)
    reason == ArchitecturalAssessment.Accepted
  }
  
  def assess(tile: Tile, request: Option[PlacedBlueprint] = None): ArchitecturalAssessment = {
    if ( ! tile.valid) {
      return ArchitecturalAssessment.Invalid
    }
    if ( ! matches(tile)) {
      return ArchitecturalAssessment.DoesntMatch
    }
    if (building.requiresPsi) {
      // We can allow use of forthcoming Pylon power by using a value higher than With.frame
      if (height == 3 && ! With.grids.psi3Height.isSet(tile) && With.architecture.powered3Height.get(tile) > With.frame) {
        return ArchitecturalAssessment.Unpowered
      }
      if (height == 2 && ! With.grids.psi2Height.isSet(tile) && With.architecture.powered2Height.get(tile) > With.frame) {
        return ArchitecturalAssessment.Unpowered
      }
    }

    val thisZone = tile.zone
    if (thisZone.island
      && ! Plasma()
      && ( ! With.blackboard.allowIslandBases() || ! thisZone.bases.exists(_.minerals.exists(_.visible)))
      && ! With.architecture.accessibleZones.contains(thisZone)) {
      return ArchitecturalAssessment.InaccessibleIsland
    }
    if (building.isGas) {
      if ( ! thisZone.bases.exists(_.gas.exists(_.tileTopLeft == tile))) {
        return ArchitecturalAssessment.IsntGas
      }
      if ( ! With.architecture.gassable(tile, request)) {
        return ArchitecturalAssessment.BlockedGas
      }
      return ArchitecturalAssessment.Accepted
    }
    if (building.isTownHall) {
      if ( ! thisZone.bases.exists(_.townHallTile == tile)) return ArchitecturalAssessment.IsntBasePosition
      if ( ! With.architecture.townhallable(tile, request)) return ArchitecturalAssessment.IsntLegalForTownHall
    }

    val buildArea = TileRectangle(tile, tile.add(width, height))
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
        if (building.requiresCreep != nextTile.creep) {
          return ArchitecturalAssessment.CreepMismatch
        }
        if ( ! building.isTownHall && ! building.isAddon && thisZone.bases.exists(_.harvestingArea.contains(nextTile))) {
          return ArchitecturalAssessment.ViolatesHarvesting
        }
        if (building.isTownHall && ! With.grids.buildableTownHall.get(nextTile)) {
          return ArchitecturalAssessment.ViolatesResourceGap
        }
        if ( ! building.isTownHall && With.grids.units.get(nextTile).exists(u => ! u.flying && u.isEnemy || ! u.canMove)) {
          return ArchitecturalAssessment.BlockedByUnit
        }
        if ( ! With.groundskeeper.isFree(tile)) {
          return ArchitecturalAssessment.Reserved
        }
        y += 1
      }
      x += 1
    }
    ArchitecturalAssessment.Accepted
  }
  
  override def toString: String = f"Blueprint: $building"
}