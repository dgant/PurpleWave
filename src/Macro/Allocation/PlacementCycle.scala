package Macro.Allocation

import Lifecycle.With
import Macro.Architecture.PlacementRequests.PlacementRequest
import Macro.Architecture.PlacementStates.{PlacementState, PlacementStateInitial}
import Performance.TaskQueue.TaskQueueGlobalWeights
import Performance.Tasks.{StateTasks, TimedTask}
import Performance.Timer

import scala.collection.mutable

class PlacementCycle extends TimedTask {

  withWeight(TaskQueueGlobalWeights.Placement)

  private val stateTasks = new StateTasks

  private val queue: mutable.ListBuffer[PlacementRequest] = new mutable.ListBuffer[PlacementRequest]
  private var state: PlacementState = new PlacementStateInitial

  override def onRun(budgetMs: Long): Unit = {
    val timer = new Timer(budgetMs)
    // Repopulate the queue if empty
    if (queue.isEmpty || state.isComplete) {
      queue ++= With.groundskeeper.suggestions
      setState(new PlacementStateInitial)
    }
    // Place each item in the queue
    while ( ! state.isComplete && timer.ongoing) {
      if (stateTasks.safeToRun(state, timer.remaining)) {
        stateTasks.run(state, state.step, timer.remaining)
      } else {
        return
      }
    }
  }

  def next: Option[PlacementRequest] = queue.headOption

  def setState(newState: PlacementState): Unit = state = newState

  def finishPlacement(request: PlacementRequest): Unit = {
    queue -= request
  }
}
