package Macro.Architecture.PlacementStates

import Lifecycle.With
import Macro.Architecture.PlacementRequests.PlacementRequest

class PlacementStateValidating(request: PlacementRequest) extends PlacementState {
  override def step() {
    if (request.policy().retain()) {
      // Recursively assume these placements
      var nextRequest: Option[PlacementRequest] = Some(request)
      while (nextRequest.nonEmpty) {
        With.placement.finishPlacement(nextRequest.get)
        nextRequest.get.tile.map(With.architecture.diffPlacement(_, request)).foreach(_.doo())
        nextRequest = nextRequest.get.child
      }
      transition(new PlacementStateReady)
    } else {
      transition(new PlacementStateEvaluating(request))
    }
  }
}
