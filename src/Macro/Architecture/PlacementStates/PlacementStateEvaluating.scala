package Macro.Architecture.PlacementStates

import Lifecycle.With
import Macro.Architecture.PlacementRequests.PlacementRequest

class PlacementStateEvaluating(request: PlacementRequest) extends PlacementState {

  val root = new PlacementNode(request)

  override def step(): Unit = {
    if ( ! root.done) {
      root.step()
    }
    if (root.done) {
      With.placement.finishPlacement(request)
      transition(new PlacementStateReady)
    }
  }
}
