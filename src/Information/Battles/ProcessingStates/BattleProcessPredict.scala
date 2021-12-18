package Information.Battles.ProcessingStates

import Information.Battles.Prediction.Skimulation.Skimulator
import Information.Battles.Types.Battle
import Lifecycle.With

class BattleProcessPredict extends BattleProcessState {

  var battles: Seq[Battle] = _

  override def step(): Unit = {
    val battles = With.battles.nextBattlesLocal

    val unpredicted = battles.find( ! _.predictionComplete)
    if (unpredicted.isDefined) {
      unpredicted.get.skimulated = With.configuration.skimulate
      if (unpredicted.get.skimulated) {
        Skimulator.predict(unpredicted.get)
      } else {
        if (With.simulation.prediction != unpredicted.get) {
          With.simulation.reset(unpredicted.get)
        }
        With.simulation.step()
      }
      return
    }

    transitionTo(new BattleProcessJudge)
  }
}
