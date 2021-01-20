package Macro.Allocation

import Lifecycle.With
import Macro.Architecture.PlacementRequests.PlacementRequest
import Macro.Architecture.PlacementStates.{PlacementState, PlacementStateInitial}
import Performance.TaskQueue.TaskQueueGlobalWeights
import Performance.Tasks.TimedTask
import Performance.Timer

import scala.collection.mutable

class PlacementCycle extends TimedTask {

  withAlwaysSafe(true)
  withWeight(TaskQueueGlobalWeights.Placement)

  private val queue: mutable.ListBuffer[PlacementRequest] = new mutable.ListBuffer[PlacementRequest]
  private var state: PlacementState = new PlacementStateInitial

  /**
    * Runs placement.
    */
  override def onRun(budgetMs: Long): Unit = {

    val timer = new Timer(budgetMs)

    // Repopulate the queue if empty
    if (queue.isEmpty || state.isComplete) {
      queue ++= With.groundskeeper.suggestions
      setState(new PlacementStateInitial)
    }

    // Place each item in the queue
    while ( ! state.isComplete && timer.ongoing) {
      state.step()
    }
  }

  def next: Option[PlacementRequest] = queue.headOption

  def setState(newState: PlacementState): Unit = state = newState

  def finishPlacement(request: PlacementRequest): Unit = {
    queue -= request
  }
}
