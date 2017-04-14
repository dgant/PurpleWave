package Performance.Tasks.Global

import Lifecycle.With
import Performance.TaskQueue.TaskQueueGrids
import Performance.Tasks.AbstractTask

class TaskGrids extends AbstractTask {
  
  override def urgency: Int = With.configuration.urgencyGrids
  
  private val taskQueue = new TaskQueueGrids
  
  override protected def onRun() {
    taskQueue.run()
  }
}
