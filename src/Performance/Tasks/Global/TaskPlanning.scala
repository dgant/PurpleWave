package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskPlanning extends AbstractTask {
  
  urgency = With.configuration.urgencyPlanning
  
  override protected def onRun() {
    With.realEstate.update()
    With.bank.update()
    With.recruiter.update()
    With.prioritizer.update()
    With.gameplan.update(null)
    With.scheduler.update()
  }
}
