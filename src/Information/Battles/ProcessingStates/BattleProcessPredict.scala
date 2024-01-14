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

      if ( ! battle.simulationComplete) {
        if (With.simulation.battle != battle) {
          With.simulation.reset(battle)
        }

        // Simulate asynchronously
        //
        if (With.configuration.simulationAsynchronous) {
          With.simulation.runAsynchronously()
        } else {
          // Simulate synchronously
          //
          With.simulation.step()
        }
      }

      // Skimulate
      //
      if ( ! battle.skimulationComplete && (battle.simulationComplete || With.simulation.simulatingAsynchronously)) {
        Skimulator.predict(battle)
        battle.skimulationComplete = true

        // For visualizing skim strength, we only want to record the local strength (as the global strength is uninteresting)
        if ( ! battle.isGlobal) {
          battle.units.foreach(u => {
            u.skimStrengthDisplay = u.skimStrength
            u.skimPresenceDisplay = u.skimPresence
          })
        }
      }

      return
    }

    transitionTo(new BattleProcessJudge)
  }
}
