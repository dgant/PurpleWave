package Macro.Allocation

import Lifecycle.With
import Macro.Architecture.PlacementStates.{PlacementState, PlacementStateInitial, PlacementStateReady}
import Macro.Architecture.{Placement, PlacementSuggestion}

import scala.collection.mutable

class PlacementCycle {

  private val queue: mutable.ListBuffer[PlacementSuggestion] = new mutable.ListBuffer[PlacementSuggestion]
  private var state: PlacementState = new PlacementStateInitial

  def update(until: Option[PlacementSuggestion] = None): Unit = {
    if (queue.isEmpty || state.isComplete) {
      queue ++= With.groundskeeper.suggestions
      setState(new PlacementStateInitial)
    }
    while (With.performance.continueRunning && ! state.isComplete) {
      state.step()
    }
  }

  def place(suggestion: PlacementSuggestion): Unit = {
    if ( ! queue.contains(suggestion)) {
      queue += suggestion
    }
    if (state.isComplete) {
      setState(new PlacementStateReady)
    }
    while (queue.contains(suggestion) && ! state.isComplete) {
      state.step()
    }
  }

  // API for placement states
  def next: Option[PlacementSuggestion] = queue.headOption
  def setState(newState: PlacementState): Unit = state = newState
  def usePlacement(placementSuggestion: PlacementSuggestion, placement: Placement): Unit = {
    //With.architecture.assumePlacement(placement)
    placementSuggestion.tile = placement.tile
    queue -= placementSuggestion
  }
}
