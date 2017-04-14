package Performance.Tasks.Global

import Lifecycle.With
import Performance.TaskQueue.TaskQueueGrids
import Performance.Tasks.AbstractTask

class TaskGrids extends AbstractTask {
  
  urgency = With.configuration.urgencyGrids
  
  private val taskQueue = new TaskQueueGrids
  
  override protected def onRun() {
    taskQueue.run()
  }
}
