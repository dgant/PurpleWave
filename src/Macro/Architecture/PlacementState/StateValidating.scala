package Macro.Architecture.PlacementState

import Macro.Architecture.Blueprint

class StateValidating(blueprint: Blueprint) extends PlacementState {
  override def step() {
    val placement = placements.get(blueprint)
  }
}
