package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskLatency extends AbstractTask {
  
  override def skippable: Boolean = false
  
  override protected def onRun() {
    With.latency.onFrame()
  }
}
