package Macro.Architecture.PlacementState

class StateReady extends PlacementState {
  override def step() {
    if (queue.isEmpty) {
      transition(new StateComplete)
    }
    else {
      transition(new StateValidating(queue.dequeue()))
    }
  }
}