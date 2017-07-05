package Macro.Architecture.PlacementState

class StateReady extends PlacementState {
  override def step() {
    if (queue.isEmpty) {
      transition(new StateComplete)
    }
    else {
      val nextBlueprint = queue.dequeue()
      transition(new StateValidating(nextBlueprint))
    }
  }
}