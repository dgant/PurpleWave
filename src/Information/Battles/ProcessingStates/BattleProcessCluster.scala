package Information.Battles.ProcessingStates

import Lifecycle.With

class BattleProcessCluster extends BattleProcessState {
  override def step(): Unit = {
    With.battles.clustering.recalculate()
    transitionTo(new BattleProcessDivisions)
  }
}
