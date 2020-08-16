package Macro.Allocation

import Lifecycle.With
import Macro.Architecture.PlacementRequests.PlacementRequest
import Macro.Architecture.PlacementStates.{PlacementState, PlacementStateInitial, PlacementStateReady}

import scala.collection.mutable

class PlacementCycle {

  private val queue: mutable.ListBuffer[PlacementRequest] = new mutable.ListBuffer[PlacementRequest]
  private var state: PlacementState = new PlacementStateInitial

  def update(until: Option[PlacementRequest] = None): Unit = {
    if (queue.isEmpty || state.isComplete) {
      queue ++= With.groundskeeper.suggestions
      setState(new PlacementStateInitial)
    }
    while (With.performance.continueRunning && ! state.isComplete) {
      state.step()
    }
  }

  def placeNow(suggestion: PlacementRequest): Unit = {
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

  def next: Option[PlacementRequest] = queue.headOption
  def setState(newState: PlacementState): Unit = state = newState
  def finishPlacement(request: PlacementRequest): Unit = {
    queue -= request
  }
}
