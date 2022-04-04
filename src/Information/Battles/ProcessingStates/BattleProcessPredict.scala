package Information.Battles.ProcessingStates

import Information.Battles.Prediction.Skimulation.Skimulator
import Information.Battles.Types.Battle
import Lifecycle.With

class BattleProcessPredict extends BattleProcessState {

  var battles: Seq[Battle] = _

  override def step(): Unit = {
    val unpredicted = With.battles.nextBattles.find( ! _.predictionComplete)
    if (unpredicted.isDefined) {
      if (unpredicted.get.skimulated) {
        Skimulator.predict(unpredicted.get)
        unpredicted.get.predictionComplete = true
      } else {
        if (With.simulation.battle != unpredicted.get) {
          With.simulation.reset(unpredicted.get)
        }
        With.simulation.step()
      }
      return
    }

    transitionTo(new BattleProcessJudge)
  }
}
