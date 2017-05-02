package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskUnitTracking extends AbstractTask {
  
  urgency = With.configuration.urgencyUnitTracking
  
  override def skippable: Boolean = false
  
  override protected def onRun() {
    With.units.update()
  }
}
