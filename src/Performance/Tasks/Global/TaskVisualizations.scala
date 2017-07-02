package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskVisualizations extends AbstractTask {
  
  override def maxConsecutiveSkips: Int = 0
  
  override protected def onRun() {
    With.visualization.render()
  }
}
