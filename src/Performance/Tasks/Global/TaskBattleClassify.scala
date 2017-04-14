package Performance.Tasks.Global

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskBattleClassify extends AbstractTask {
  
  override def urgency: Int = With.configuration.urgencyBattles
  
  override protected def onRun() {
    With.battles.classify()
  }
}
