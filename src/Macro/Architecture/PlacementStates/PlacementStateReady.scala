package Macro.Architecture.PlacementStates

class PlacementStateReady extends PlacementState {
  override def step() {
    if (queue.isEmpty) {
      transition(new PlacementStateComplete)
    }
    else {
      val nextBlueprint = queue.dequeue()
      transition(new PlacementStateValidating(nextBlueprint))
    }
  }
}