package Performance.TaskQueue

import Lifecycle.With
import Performance.Tasks.AbstractTask

abstract class AbstractTaskQueue {
  
  val tasks: Vector[AbstractTask]
  
  def run()
  {
    // There's some path-dependency in task initialization.
    // Run each task in order until we've run everything once.
    //
    if (tasks.exists(_.hasNeverRun)) {
      while (With.performance.continueRunning && tasks.exists(_.hasNeverRun)) {
        tasks.find(_.hasNeverRun).get.run()
      }
      return
    }
  
    tasks
      .sortBy(task => - task.urgency * task.framesSinceRunning)
      .sortBy( ! _.due)
      
  
    // Ordinarily we'd do foreach() but that swallows exceptions and I don't understand why
    //
    var i = 0
    while (i < tasks.length) {
      val task = tasks(i)
      val expectedMilliseconds =
        Math.max(
          if (task.totalRuns < 10) 5 else 0, // Arbitrary assumption before we have much data
          if (With.performance.danger) task.runMillisecondsMaxAllTime else 2 * task.runMillisecondsMaxRecent())
    
      if (task.framesSinceRunning > task.maxConsecutiveSkips
          || With.performance.millisecondsLeftThisFrame > expectedMilliseconds
          || ! With.performance.enablePerformancePauses) {
        task.run()
      } else {
        task.skip()
      }
      i += 1
    }
  }

  def statusTable: Vector[Vector[String]] = {
        val title = Vector("Cutoff: ", With.configuration.targetFrameDurationMilliseconds + "ms")
    val headers = Vector("Task", "Last run", " Run %", "Seconds", "Avg ms", "Max (Recent)", "Max (All time)", "Extended", "Disqualifying")
    val body = With.tasks.tasks
      .sortBy(_.getClass.getSimpleName)
      .map(task => Vector(
        task.getClass.getSimpleName.replace("Task", ""),
        "X" * Math.min(10, Math.max(0, task.framesSinceRunning - 1)),
        " " + (100 * (1.0 + task.totalRuns) / (1.0 + task.totalSkips + task.totalRuns)).toInt.toString + "%%",
        (task.runMillisecondsTotal / 1000).toString,
        task.runMillisecondsMean.toString,
        task.runMillisecondsMaxRecent().toString,
        task.runMillisecondsMaxAllTime.toString,
        task.totalViolatedThreshold.toString,
        task.totalViolatedRules.toString
      ))
    Vector(title) ++ Vector(headers) ++ body
  }

  def status: String = statusTable.map(_.mkString("\t")).mkString("\n")

  def onEnd(): Unit = {
    With.logger.debug(status)
    tasks.foreach(_.onEnd())
  }
}
