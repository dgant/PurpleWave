package Macro.Architecture.PlacementState

import Lifecycle.With
import Macro.Architecture.{Architect, Blueprint}

class StateValidating(blueprint: Blueprint) extends PlacementState {
  override def step() {
    val placement = placements.get(blueprint)
    
    val validatedPlacement = Architect.validate(blueprint, placement)
    if (validatedPlacement.isDefined) {
      With.architecture.assumePlacement(validatedPlacement.get)
      transition(new StateReady)
    }
    else {
      transition(new StateEvaluating(blueprint))
    }
  }
}
