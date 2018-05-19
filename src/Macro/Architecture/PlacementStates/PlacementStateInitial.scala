package Macro.Architecture.PlacementStates

import Lifecycle.With

class PlacementStateInitial extends PlacementState {
  override def step() {
    With.architecture.reboot()
    transition(new PlacementStateReady)
  }
}
