package Performance.Tasks.Global

import Information.Battles.Estimation.GroupEstimator
import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGroupEstimation extends AbstractTask {
  
  urgency = With.configuration.urgencyBattles
  
  override protected def onRun() {
    GroupEstimator.run()
  }
}
