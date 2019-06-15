package Macro.Architecture.PlacementStates

import Lifecycle.With
import Macro.Architecture.{PlacementResult, PlacementRequest}
import Mathematics.Points.Tile

import scala.collection.mutable

class PlacementStateValidating(request: PlacementRequest) extends PlacementState {
  override def step() {
    if (request.tile.exists(request.blueprint.accepts(_, Some(request)))
      && ! request.blueprint.requireTownHallTile.contains(true) // Town halls are so important we should always recalculate
      && request.placementResult.forall(result => With.framesSince(result.frameFinished) < With.configuration.buildingPlacementRefreshPeriod)) {
      val result = request.placementResult.getOrElse(PlacementResult(
        request,
        request.tile,
        Seq.empty,
        new mutable.HashMap[Tile, Double],
        totalNanoseconds  = 0,
        frameStarted      = With.frame,
        frameFinished     = With.frame,
        candidates        = 1,
        evaluated         = 1))
      With.placement.usePlacement(request, result)
      transition(new PlacementStateReady)
    } else {
      transition(new PlacementStateEvaluating(request))
    }
  }
}
