package Information.Battles.ProcessingStates

import Lifecycle.With

class BattleProcessSimulate extends BattleProcessState {
  override def step(): Unit = {
    // TODO: Break up
    With.battles.nextBattlesLocal.foreach(_.estimate())

    // TODO: When done
    transitionTo(new BattleProcessJudge)
  }
}
