package Macro.Architecture.PlacementStates

import Lifecycle.With
import Macro.Architecture.{Architect, Blueprint}

class PlacementStateValidating(blueprint: Blueprint) extends PlacementState {
  override def step() {
    val placement = placements.get(blueprint)
    val validatedPlacement = Architect.validate(blueprint, placement)
    if (validatedPlacement.isDefined) {
      With.architecture.assumePlacement(validatedPlacement.get)
      transition(new PlacementStateReady)
    }
    else {
      transition(new PlacementStateEvaluating(blueprint))
    }
  }
}
