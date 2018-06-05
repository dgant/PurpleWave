package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskPlanning extends AbstractTask {
  
  urgency = With.configuration.urgencyPlanning
  
  override protected def onRun() {
    With.intelligence.update()
    With.bank.update()
    With.recruiter.update()
    With.prioritizer.update()
    With.scheduler.reset()
    With.squads.reset()
    With.buildOrderHistory.update()
    With.strategy.gameplan.update()
    With.groundskeeper.update()
    With.squads.update()
  }
}
