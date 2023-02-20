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

        // For visualizing skim strength, we only want to record the local strength (as the global strength is uninteresting)
        battle.units.foreach(u => u.skimStrengthDisplay = u.skimStrength)
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
