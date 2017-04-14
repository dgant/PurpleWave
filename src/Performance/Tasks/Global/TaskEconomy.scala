package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskEconomy extends AbstractTask {
  
  urgency = With.configuration.urgencyEconomy
  
  override protected def onRun() {
    With.economy.update()
  }
}
