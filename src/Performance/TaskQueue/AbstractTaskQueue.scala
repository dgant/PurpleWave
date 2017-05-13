package Performance.TaskQueue

import Lifecycle.With
import Performance.Tasks.AbstractTask

abstract class AbstractTaskQueue {
  
  val tasks:Vector[AbstractTask]
  
  def run() {
    if (With.frame == 0) {
      tasks.foreach(_.run())
    } else {
      var definitelyRunNextTask = true
      tasks
        .sortBy(system => - system.urgency * system.framesSinceRunning)
        .sortBy(system => system.skippable)
        
      //Ordinarily we'd do foreach() but that swallows exceptions and I don't understand why
      var i = 0
      while (i < tasks.length) {
        val task = tasks(i)
        if (definitelyRunNextTask       ||
          ! task.skippable              ||
          task.framesSinceRunning > 24  ||
          With.performance.millisecondsLeftThisFrame > Math.min(task.runMillisecondsMaxRecent, With.configuration.peformanceFrameMilliseconds)) {
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
