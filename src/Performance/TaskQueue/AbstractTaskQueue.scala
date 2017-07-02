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
    if (tasks.exists(_.totalRuns == 0)) {
      while (With.performance.continueRunning) {
        tasks.find(_.totalRuns == 0).get.run()
      }
      return
    }
  
    // Ordinarily we'd do foreach() but that swallows exceptions and I don't understand why
    //
    var i = 0
    while (i < tasks.length) {
      val task = tasks(i)
      val expectedMilliseconds =
        Math.max(
          if (task.totalRuns < 10) With.configuration.initialTaskLengthMilliseconds else 0,
          if (With.performance.danger) task.runMillisecondsMaxAllTime else task.runMillisecondsMaxRecent)
    
      if (
        task.framesSinceRunning > task.maxConsecutiveSkips
          || With.performance.millisecondsLeftThisFrame > expectedMilliseconds) {
        task.run()
      } else {
        task.skip()
      }
      i += 1
    }
  }
}
