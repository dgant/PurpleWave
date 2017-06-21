package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskBattles extends AbstractTask {
  
  urgency = With.configuration.urgencyBattles
  
  override protected def onRun() {
    With.battles.run()
  }
}
