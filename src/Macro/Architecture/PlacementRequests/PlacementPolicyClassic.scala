package Macro.Architecture.PlacementRequests

import Lifecycle.With
import Macro.Architecture.Tiles.Surveyor
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

class PlacementPolicyClassic(request: PlacementRequest) extends PlacementPolicy {

  override def retain(): Boolean = {
    (request.tile.exists(request.blueprint.accepts(_, Some(request)))
      && request.blueprint.requireTownHallTile.contains(false) // Town halls are so important (and inexpensive) that we should always recalculate
      && request.result.forall(result => With.framesSince(result.frameFinished) < With.configuration.buildingPlacementRefreshPeriod))
  }

  override def tiles: Seq[Tile] = {
    Surveyor.candidates(request.blueprint).view.flatMap(_.tiles(request.blueprint))
  }

  override def accept(tile: Tile): Boolean = {
    request.blueprint.accepts(tile, Some(request))
  }

  override def score(tile: Tile): Double = {
    HeuristicMathMultiplicative.resolve(
      request.blueprint,
      request.blueprint.placement.get.weightedHeuristics,
      tile)
  }
}
