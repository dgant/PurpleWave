package Information.Battles.ProcessingStates

import Information.Battles.Types.BattleJudgment
import Lifecycle.With

class BattleProcessJudge extends BattleProcessState {
  override def step(): Unit = {
    // TODO -- Split up
    With.battles.nextBattlesLocal.foreach(battle => battle.judgement = Some(new BattleJudgment(battle)))

    // TODO -- only when done
    With.battles.measureReactionTime()

    transitionTo(new BattleProcessSwap)
  }
}
