package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGeography extends AbstractTask {
  
  override def urgency: Int = With.configuration.urgencyGeography
  
  override protected def onRun() {
   With.geography.update()
  }
}
