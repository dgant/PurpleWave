package Performance.TaskQueue

import Lifecycle.With
import Performance.Tasks.TimedTask

/**
  * Runs a group of tasks with weighted priority
  */
class TaskQueueParallel(val tasks: TimedTask*) extends TimedTask {

  withAlwaysSafe(true)

  // Not actually sure when we'd ever check this for a parallel queue,
  // since the expectation is that this will run indefinitely.
  override def isComplete: Boolean = tasks.forall(_.isComplete)

  override def onRun()
  {
    // Run each task in order until we've run everything once.
    // This is because the global task queue needs to be initialized in the given order
    if (tasks.exists(_.hasNeverRun)) {
      while (With.performance.continueRunning && tasks.exists(_.hasNeverRun)) {
        tasks.find(_.hasNeverRun).get.run()
      }
      return
    }
  
    val tasksSorted = tasks
      .sortBy(task => - task.urgency * task.framesSinceRunning)
      .sortBy( ! _.due) // Ensure we run any due tasks
      .sortBy(_.cosmetic) // Don't let a cosmetic task overtake a due task

    var i = 0
    while (i < tasksSorted.length) {
      val task = tasksSorted(i)
      if (i == 0 || task.safeToRun) {
        task.run()
      }
      i += 1
    }

    if (With.performance.violatedLimit) {
      With.logger.debug(f"$toString crossed the ${With.configuration.frameMillisecondLimit}ms limit to {With.performance.millisecondsSpentThisFrame}ms. Task durations: ${
        tasksSorted
          .filter(_.framesSinceRunning <= 1)
          .sortBy(- _.runMsLast)
          .map(t => f"(${t.runMsLast}ms: ${t.toString})")
          .mkString(" ")
      }")
    }
  }
}
