package Performance.TaskQueue

import Lifecycle.With
import Mathematics.PurpleMath
import Performance.Tasks.TimedTask
import Performance.Timer

/**
  * Runs a group of tasks with weighted priority
  */
class TaskQueueParallel(val tasks: TimedTask*) extends TimedTask {

  withAlwaysSafe(true)

  // Not actually sure when we'd ever check this for a parallel queue,
  // since the expectation is that this will run indefinitely.
  override def isComplete: Boolean = tasks.forall(_.isComplete)

  override def onRun(budgetMs: Long)
  {
    val timer = new Timer(budgetMs)

    // Run each task in order until we've run everything once.
    // This is because the global task queue needs to be initialized in the given order
    if (tasks.exists(_.hasNeverRun)) {
      while (With.performance.continueRunning && tasks.exists(_.hasNeverRun)) {
        tasks.find(_.hasNeverRun).get.run(With.performance.msBeforeTarget)
      }
      return
    }

    val taskWeightTotal = tasks.view.map(_.weight).sum
    val tasksSorted = tasks
      .sortBy(task => task.runMsRecentTotal() / Math.max(1, task.weight))
      .sortBy( ! _.due) // Ensure we run any due tasks
      .sortBy(_.cosmetic) // Don't let a cosmetic task overtake a due task

    var i = 0
    while (i < tasksSorted.length) {
      val task                = tasksSorted(i)
      val budgetRatio         = task.weight.toDouble / taskWeightTotal
      val budgetMsPerFrame    = With.configuration.frameTargetMs * budgetRatio
      val budgetMsRecent      = budgetMsPerFrame * task.runMsSamplesMax
      val budgetMsRecentSpent = task.runMsRecentTotal()
      val taskBudgetMs        = PurpleMath.clamp(budgetMsRecent - budgetMsRecentSpent, 0, With.performance.msBeforeTarget).toLong

      if (i == 0 || (task.due && ! With.performance.danger) || (timer.ongoing && task.safeToRun(taskBudgetMs))) {
        task.run(taskBudgetMs)
      } else {
        task.skip()
      }
      i += 1
    }

    if (With.performance.violatedLimit) {
      With.logger.debug(f"$toString crossed ${With.configuration.frameLimitMs}ms to ${With.performance.frameMs}ms. Task durations: ${
        tasksSorted
          .filter(_.framesSinceRunning <= 1)
          .sortBy(- _.runMsLast)
          .map(t => f"(${t.runMsLast}ms: ${t.toString})")
          .mkString(" ")
      }")
    }
  }
}
