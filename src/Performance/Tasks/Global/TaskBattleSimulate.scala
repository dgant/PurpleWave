package Performance.Tasks.Global

import Information.Battles.Simulation.BattleSimulator
import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskBattleSimulate extends AbstractTask {
  
  override def urgency: Int = With.configuration.urgencyBattles
  
  override protected def onRun() {
    BattleSimulator.run()
  }
}
