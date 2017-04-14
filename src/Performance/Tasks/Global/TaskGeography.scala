package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGeography extends AbstractTask {
  
  urgency = With.configuration.urgencyGeography
  
  override protected def onRun() {
   With.geography.update()
  }
}
