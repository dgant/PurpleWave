package Macro.Architecture.PlacementStates

class PlacementStateReady extends PlacementState {
  override def step() {
    if (next.isEmpty) {
      transition(new PlacementStateComplete)
    } else {
      transition(new PlacementStateValidating(next.get))
    }
  }
}