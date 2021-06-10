package Information.Battles.ProcessingStates

import Information.Battles.Types.Battle
import Lifecycle.With

class BattleProcessSimulate extends BattleProcessState {

  var battles: Seq[Battle] = _

  override def step(): Unit = {
    val battles = With.battles.nextBattlesLocal

    val unsimulated = battles.find( ! _.simulationComplete)
    if (unsimulated.isDefined) {
      if (With.simulation.prediction != unsimulated.get) {
        With.simulation.reset(unsimulated.get)
      }
      With.simulation.step()
      return
    }


    transitionTo(new BattleProcessJudge)
  }
}
