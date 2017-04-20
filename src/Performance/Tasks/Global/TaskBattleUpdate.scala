package Performance.Tasks.Global

import Information.Battles.BattleUpdater
import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskBattleUpdate extends AbstractTask {
  
  urgency = With.configuration.urgencyBattles
  
  override protected def onRun() {
    BattleUpdater.run()
  }
}
