package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskMicro extends AbstractTask {
  
  urgency = With.configuration.urgencyMicro
  
  override protected def onRun() {
    With.commander.run()
    With.executor.run()
  }
}
