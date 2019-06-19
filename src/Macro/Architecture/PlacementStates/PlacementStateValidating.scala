package Macro.Architecture.PlacementStates

import Lifecycle.With
import Macro.Architecture.PlacementRequests.{PlacementRequest, PlacementResult}

class PlacementStateValidating(request: PlacementRequest) extends PlacementState {
  override def step() {
    if (request.tile.exists(request.blueprint.accepts(_, Some(request)))
      && ! request.blueprint.requireTownHallTile.contains(true) // Town halls are so important we should always recalculate
      && request.result.forall(result => With.framesSince(result.frameFinished) < With.configuration.buildingPlacementRefreshPeriod)) {
      val result = request.result.getOrElse(PlacementResult(
        request,
        request.tile,
        totalNanoseconds  = 0,
        frameStarted      = With.frame,
        frameFinished     = With.frame,
        candidates        = 1,
        evaluated         = 1))
      // TODO: Need to handle recursive placements -- this state should be removed, and identifying still-good tiles should move to the placement tasks
      With.placement.finishPlacement(request)
      With.architecture.diffPlacement(result).doo()
      transition(new PlacementStateReady)
    } else {
      transition(new PlacementStateEvaluating(request))
    }
  }
}
