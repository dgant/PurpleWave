package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskUnitTracking extends AbstractTask {
  
  override def urgency: Int = With.configuration.urgencyUnitTracking
  
  override protected def onRun() {
    With.units.update()
  }
}
