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
          if (task.totalRuns < 10) With.configuration.initialTaskLengthMilliseconds else 0,
          if (With.performance.danger) task.runMillisecondsMaxAllTime else 2 * task.runMillisecondsMaxRecent)
    
      if (task.framesSinceRunning > task.maxConsecutiveSkips
          || With.performance.millisecondsLeftThisFrame > expectedMilliseconds
          || ! With.performance.enablePerformanceStops) {
        task.run()
      } else {
        task.skip()
      }
      i += 1
    }
    
    if (With.performance.millisecondsSpentThisFrame < With.configuration.garbageCollectionThresholdMs) {
      System.gc()
    }
  }
}
