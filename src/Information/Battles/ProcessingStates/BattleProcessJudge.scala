package Information.Battles.ProcessingStates

import Lifecycle.With

class BattleProcessJudge extends BattleProcessState {
  override def step(): Unit = {
    // TODO


    // TODO
    With.battles.measureReactionTime()

    transitionTo(new BattleProcessComplete)
  }
}
