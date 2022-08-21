package Information.Battles.ProcessingStates

import Information.Battles.Prediction.Skimulation.Skimulator
import Information.Battles.Types.Battle
import Lifecycle.With

class BattleProcessPredict extends BattleProcessState {

  var battles: Seq[Battle] = _

  override def step(): Unit = {
    val unpredicted = With.battles.nextBattles.find( ! _.predictionComplete)
    if (unpredicted.isDefined) {
      val battle = unpredicted.get
      battle.simulationComplete   ||= ! battle.simulated
      battle.skimulationComplete  ||= ! battle.skimulated

      if (battle.predictionComplete) return

      if ( ! battle.skimulationComplete) {
        Skimulator.predict(battle)
        battle.skimulationComplete = true
      } else {
        if (With.simulation.battle != battle) {
          With.simulation.reset(battle)
        }
        With.simulation.step()
      }
      return
    }

    transitionTo(new BattleProcessJudge)
  }
}
