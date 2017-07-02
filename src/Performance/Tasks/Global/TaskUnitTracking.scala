package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskUnitTracking extends AbstractTask {
  
  urgency = With.configuration.urgencyUnitTracking
  
  override def maxConsecutiveSkips: Int = 0
  
  override protected def onRun() {
    With.units.update()
  }
}
