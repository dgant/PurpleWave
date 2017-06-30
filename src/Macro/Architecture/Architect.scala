package Macro.Architecture

import Lifecycle.With
import Macro.Architecture.Heuristics.EvaluatePlacements
import Mathematics.Points.{Tile, TileRectangle}

object Architect {
  
  def fulfill(blueprint: Blueprint, existingPlacement: Option[Placement]): Placement = {
    
    val placement = validate(blueprint, existingPlacement).getOrElse(place(blueprint))
    With.architecture.assumePlacement(placement)
    placement
  }
  
  def validate(blueprint: Blueprint, placement: Option[Placement]): Option[Placement] = {
    val placementHasTile = placement.exists(_.tile.isDefined)
    val placementIsFresh = placement.exists(With.frame - _.createdFrame < With.configuration.maxPlacementAge)
  
    if (placementHasTile && placementIsFresh) {
      if (canBuild(blueprint, placement.get.tile.get)) {
        With.architecture.assumePlacement(placement.get)
        return placement
      }
    }
    None
  }
  
  def canBuild(blueprint: Blueprint, tile: Tile): Boolean = {
    lazy val buildArea = TileRectangle(
      tile.add(blueprint.relativeBuildStart),
      tile.add(blueprint.relativeBuildEnd))
    
    blueprint.accepts(tile)                       &&
    ! intersectsExclusion (blueprint, buildArea)  &&
    ! intersectsUnits     (blueprint, buildArea)  &&
    ! With.architecture.affectsPathing(buildArea)
  }
  
  private def intersectsExclusion(blueprint: Blueprint, buildArea: TileRectangle): Boolean = {
    With.architecture.exclusions
      .exists(exclusion =>
        ! (exclusion.gasAllowed       && blueprint.gas)       &&
        ! (exclusion.townHallAllowed  && blueprint.townHall)  &&
        exclusion.areaExcluded.intersects(buildArea))
  }
  
  private def intersectsUnits(blueprint: Blueprint, buildArea: TileRectangle): Boolean = {
    if (blueprint.gas) return false
    var totalWorkers = 0
    buildArea.tiles.foreach(tile =>
      With.grids.units.get(tile).foreach(unit =>
        if (unit.targetPixel.exists(targetPixel => ! buildArea.contains(targetPixel.tileIncluding))) {
          //This is fine. Right?
          //We want to be a little lenient about units being in the way because otherwise we often refuse to take our natural
        }
        else if (unit.isOurs && unit.unitClass.isWorker) {
          totalWorkers += 1
          if (totalWorkers > 1) {
            return true
          }
        }
        else if ( ! unit.flying) {
          return true
        }
      ))
    false
  }
  
  private def place(blueprint: Blueprint): Placement = {
    EvaluatePlacements.best(
      blueprint,
      Surveyor.candidates(blueprint)
        .filter(canBuild(blueprint, _))
        .take(With.configuration.maxGroundskeeperSearches))
  }
}
