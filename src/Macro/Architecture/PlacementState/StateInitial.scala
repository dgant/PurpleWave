package Macro.Architecture.PlacementState

import Lifecycle.With

class StateInitial extends PlacementState {
  override def step() {
    With.architecture.reboot()
    transition(new StateReady)
  }
}
