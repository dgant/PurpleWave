package Macro.Architecture.PlacementStates

import Lifecycle.With
import Macro.Architecture.Blueprint

class PlacementStateValidating(blueprint: Blueprint) extends PlacementState {
  override def step() {
    val placement = placements.get(blueprint)
    if (placement.exists(_.satisfies(blueprint))) {
      With.architecture.assumePlacement(placement.get)
      transition(new PlacementStateReady)
    }
    else {
      transition(new PlacementStateEvaluating(blueprint))
    }
  }
}
