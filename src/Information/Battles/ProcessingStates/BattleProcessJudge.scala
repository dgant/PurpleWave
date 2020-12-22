package Information.Battles.ProcessingStates

import Information.Battles.Types.NewBattleJudgment
import Lifecycle.With

class BattleProcessJudge extends BattleProcessState {
  override def step(): Unit = {

    val unjudged = With.battles.nextBattlesLocal.find(_.judgement.isEmpty)

    if (unjudged.isDefined) {
      unjudged.get.judgement = Some(new NewBattleJudgment(unjudged.get))
      return
    }

    With.battles.measureReactionTime()

    transitionTo(new BattleProcessSwap)
  }
}
