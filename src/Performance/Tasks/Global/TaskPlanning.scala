package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskPlanning extends AbstractTask {
  
  urgency = With.configuration.urgencyPlanning
  
  override protected def onRun() {
    With.bank.update()
    With.recruiter.update()
    With.prioritizer.update()
    With.gameplan.update()
    With.scheduler.update()
    With.groundskeeper.update()
  }
}
