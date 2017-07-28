package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskMicro extends AbstractTask {
  
  urgency = With.configuration.urgencyMicro
  
  override def maxConsecutiveSkips: Int = 1
  
  override protected def onRun() {
    With.matchups.run()
    With.commander.run()
    With.agents.run()
  }
}
