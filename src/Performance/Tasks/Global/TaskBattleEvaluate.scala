package Performance.Tasks.Global

import Information.Battles.Evaluation.BattleEvaluator
import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskBattleEvaluate extends AbstractTask {
  
  urgency = With.configuration.urgencyBattles
  
  override protected def onRun() {
    BattleEvaluator.run()
  }
}
