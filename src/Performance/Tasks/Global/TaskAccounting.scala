package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskAccounting extends AbstractTask {
  
  urgency = With.configuration.urgencyEconomy
  
  override protected def onRun() {
    With.economy.update()
  }
}
