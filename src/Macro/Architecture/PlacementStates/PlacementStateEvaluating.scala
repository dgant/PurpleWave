package Macro.Architecture.PlacementStates

import Lifecycle.With
import Macro.Architecture.PlacementRequests.PlacementRequest

class PlacementStateEvaluating(request: PlacementRequest) extends PlacementState {
  val task = request.task()

  override def step(): Unit = {
    val placement = task.step()
    placement.foreach(result => {
      With.placement.usePlacement(request, result)
      transition(new PlacementStateReady)
    })
  }
}
