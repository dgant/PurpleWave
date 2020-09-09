package Macro.Allocation

import Lifecycle.With
import Macro.Architecture.PlacementRequests.PlacementRequest
import Macro.Architecture.PlacementStates.{PlacementState, PlacementStateInitial}

import scala.collection.mutable

class PlacementCycle {

  private val queue: mutable.ListBuffer[PlacementRequest] = new mutable.ListBuffer[PlacementRequest]
  private var state: PlacementState = new PlacementStateInitial

  /**
    * Runs placement.
    */
  def update(): Unit = {

    // Repopulate the queue if empty
    if (queue.isEmpty || state.isComplete) {
      queue ++= With.groundskeeper.suggestions
      setState(new PlacementStateInitial)
    }

    // Place each item in the queue
    while (With.performance.continueRunning && ! state.isComplete) {
      state.step()
    }
  }

  def next: Option[PlacementRequest] = queue.headOption

  def setState(newState: PlacementState): Unit = state = newState

  def finishPlacement(request: PlacementRequest): Unit = {
    queue -= request
  }
}
