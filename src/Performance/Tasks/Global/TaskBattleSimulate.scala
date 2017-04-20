package Performance.Tasks.Global

import Information.Battles.Simulation.Execution.BattleSimulator
import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskBattleSimulate extends AbstractTask {
  
  urgency = With.configuration.urgencyBattles
  
  override protected def onRun() {
    BattleSimulator.run()
  }
}
