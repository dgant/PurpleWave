package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskArchitecture extends AbstractTask {
  
  urgency = With.configuration.urgencyBuildingPlacement
  
  override protected def onRun() {
    With.placement.run()
  }
}
