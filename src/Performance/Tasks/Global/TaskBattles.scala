package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskBattles extends AbstractTask {
  
  urgency = With.configuration.urgencyBattles
  
  override def maxConsecutiveSkips: Int = 8
  
  override protected def onRun() {
    With.battles.run()
  }
}
