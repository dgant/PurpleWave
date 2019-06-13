package Macro.Architecture.PlacementStates

import Lifecycle.With
import Macro.Architecture.PlacementSuggestion

abstract class PlacementState {
  final def next: Option[PlacementSuggestion] = With.placement.next
  final def transition(state: PlacementState): Unit = With.placement.setState(state)
  
  def isComplete: Boolean = false
  def step() {}
}
