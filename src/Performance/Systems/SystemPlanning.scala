package Performance.Systems

import Lifecycle.With

class SystemPlanning extends AbstractSystem {
  
  override def urgency: Int = With.configuration.urgencyPlanning
  
  override protected def onRun() {
    With.realEstate.update()
    With.bank.update()
    With.recruiter.update()
    With.prioritizer.update()
    With.gameplan.update()
    With.scheduler.update()
  }
}
