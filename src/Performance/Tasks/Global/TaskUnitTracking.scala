package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskUnitTracking extends AbstractTask {
  
  urgency = With.configuration.urgencyUnitTracking
  
  override protected def onRun() {
    With.units.update()
  }
}
