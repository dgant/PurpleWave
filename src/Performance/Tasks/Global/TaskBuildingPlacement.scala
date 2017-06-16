package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskBuildingPlacement extends AbstractTask {
  
  urgency = With.configuration.urgencyBuildingPlacement
  
  override protected def onRun() {
    With.groundskeeper.run()
  }
}
