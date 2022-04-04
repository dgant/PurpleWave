package Information.Battles.ProcessingStates

import Information.Battles.Types.BattleJudgment
import Lifecycle.With

class BattleProcessJudge extends BattleProcessState {
  override def step(): Unit = {

    val unjudged = With.battles.nextBattles.find(_.judgement.isEmpty)

    if (unjudged.isDefined) {
      unjudged.get.judgement = Some(new BattleJudgment(unjudged.get))
      return
    }

    With.battles.measureReactionTime()

    transitionTo(new BattleProcessSwap)
  }
}
