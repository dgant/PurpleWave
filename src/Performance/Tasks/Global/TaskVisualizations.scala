package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskVisualizations extends AbstractTask {
  
  override def skippable: Boolean = false
  
  override protected def onRun() {
    With.visualization.render()
  }
}
