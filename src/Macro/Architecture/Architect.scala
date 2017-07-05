package Macro.Architecture

import Lifecycle.With
import Mathematics.Points.{Tile, TileRectangle}

object Architect {
  
  def validate(blueprint: Blueprint, placement: Option[Placement]): Option[Placement] = {
    val placementHasTile = placement.exists(_.tile.isDefined)
    val placementIsFresh = placement.exists(p => With.framesSince(p.createdFrame) < With.configuration.maxPlacementAge)
  
    if (placementHasTile && placementIsFresh) {
      if (canBuild(blueprint, placement.get.tile.get)) {
        return placement
      }
    }
    None
  }
  
  def canBuild(blueprint: Blueprint, tile: Tile): Boolean = {
    lazy val buildArea = TileRectangle(
      tile.add(blueprint.relativeBuildStart),
      tile.add(blueprint.relativeBuildEnd))
    
    if ( ! blueprint.accepts(tile)) {
      return false
    }
    
    if (With.configuration.verifyBuildingsDontBreakPaths && ! blueprint.margin && With.architecture.breaksPathing(buildArea)) {
      return false
    }
    
    true
  }
}
