package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskLatency extends AbstractTask {
  
  override def maxConsecutiveSkips: Int = 0
  
  override protected def onRun() {
    With.latency.onFrame()
  }
}
