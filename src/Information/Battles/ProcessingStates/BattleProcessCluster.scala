package Information.Battles.ProcessingStates

import Lifecycle.With

class BattleProcessCluster extends BattleProcessState {
  override def step(): Unit = {
    With.battles.clustering.recalculate()
    With.battles.nextBattlesLocal.foreach(_.combatForecasts)
    transitionTo(new BattleProcessDivisions)
  }
}
