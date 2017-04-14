package Performance.TaskQueue

import Lifecycle.With
import Performance.Tasks.AbstractTask

abstract class AbstractTaskQueue {
  
  val tasks:Vector[AbstractTask]
  
  def run() {
    if (With.frame == 0) {
      tasks.foreach(_.run())
    } else {
      var definitelyRunNextSystem = true
      tasks
        .sortBy(system => - system.urgency * system.framesSinceRunning)
        .sortBy(system => system.skippable)
        .foreach(system =>
          if (definitelyRunNextSystem || ! system.skippable || With.performance.millisecondsLeftThisFrame > system.runMillisecondsMax) {
            if (system.skippable) {
              definitelyRunNextSystem  = false
            }
            system.run()
          } else {
            system.skip()
          })
    }
  }
}
