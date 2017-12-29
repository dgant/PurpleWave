package Macro.Architecture

import Lifecycle.With
import Mathematics.Points.{Tile, TileRectangle}

object Architect {
  
  def validate(blueprint: Blueprint, placement: Option[Placement]): Option[Placement] = {
    val placementHasTile = placement.exists(_.tile.isDefined)
    val placementIsFresh = placement.exists(p => With.framesSince(p.frameFinished) < With.configuration.maxPlacementAgeFrames)
  
    if (placementHasTile && placementIsFresh) {
      if (canBuild(blueprint, placement.get.tile.get, recheckPathing = false)) {
        return placement
      }
    }
    None
  }
  
  def canBuild(blueprint: Blueprint, tile: Tile, recheckPathing: Boolean = false): Boolean = {
    lazy val buildArea = TileRectangle(
      tile.add(blueprint.relativeBuildStart),
      tile.add(blueprint.relativeBuildEnd))
    
    if ( ! blueprint.accepts(tile)) {
      return false
    }
    
    if (With.configuration.buildingPlacementTestsPathing && recheckPathing && With.architecture.breaksPathing(buildArea)) {
      return false
    }
    
    true
  }
}
