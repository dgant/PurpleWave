package Performance.TaskQueue

import Lifecycle.With
import Performance.Tasks.AbstractTask

abstract class AbstractTaskQueue {
  
  val tasks: Vector[AbstractTask]
  
  def run() {
    if (With.frame == -1) { // CHANGED! Let's see how this goes
      tasks.foreach(_.run())
    } else {
      
      var definitelyRunNextTask = true
      
      // There's some path-dependency in task initialization.
      // Run each task in order until we've run everything once.
      //
      if (tasks.forall(_.totalRuns > 0)) {
        tasks
          .sortBy(task => - task.urgency * task.framesSinceRunning)
          .sortBy(task => task.skippable)
      }
        
      // Ordinarily we'd do foreach() but that swallows exceptions and I don't understand why
      //
      var i = 0
      
      while (i < tasks.length) {
        val task = tasks(i)
        val expectedMilliseconds =
          Math.max(
            if (task.totalRuns < 10)      With.configuration.initialTaskLengthMilliseconds else 0,
            if (With.performance.danger)  task.runMillisecondsMaxAllTime else task.runMillisecondsMaxRecent)
        
        if (definitelyRunNextTask       ||
          ! task.skippable              ||
          task.framesSinceRunning > 24  ||
          With.performance.millisecondsLeftThisFrame > expectedMilliseconds) {
          if (task.skippable) {
            definitelyRunNextTask = false
          }
          task.run()
        } else {
          task.skip()
        }
        i += 1
      }
      
    }
  }
}
