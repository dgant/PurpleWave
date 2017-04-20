package Performance.Tasks.Global

import Information.Battles.Estimation.BattleEstimator
import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskBattleEstimator extends AbstractTask {
  
  urgency = With.configuration.urgencyBattles
  
  override protected def onRun() {
    BattleEstimator.run()
  }
}
