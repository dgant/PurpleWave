package Information.Battles.ProcessingStates

import Lifecycle.With

class BattleProcessCluster extends BattleProcessState {

  private var needToReset: Boolean = true

  override def step(): Unit = {

    if (needToReset) {
      needToReset = false
      With.battles.clustering.reset()
    }

    if ( ! With.battles.clustering.isComplete) {
      With.battles.clustering.step()
    } else {
      With.battles.clustering.publish()
      transitionTo(new BattleProcessSimulate)
    }
  }
}
