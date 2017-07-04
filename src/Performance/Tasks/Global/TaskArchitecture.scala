package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskArchitecture extends AbstractTask {
  
  urgency = With.configuration.urgencyArchitecture
  
  override protected def onRun() {
    With.placement.run()
  }
}
