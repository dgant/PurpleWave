package Information.Battles.ProcessingStates

import Information.Battles.Prediction.Skimulation.Skimulator
import Information.Battles.Types.Battle
import Lifecycle.With

import scala.concurrent.Promise

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
          if (With.simulation.future.isEmpty) {
            val promise             = Promise[Unit]()
            val priorityNow         = Thread.currentThread().getPriority
            val prioritySim         = Math.max(priorityNow - 1, Thread.MIN_PRIORITY)
            With.simulation.future  = Some(promise.future)
            val thread              = new Thread(() => {
              while ( ! With.simulation.battle.simulationComplete) {
                With.simulation.step()
              }
              promise.success(())
            })
            thread.setPriority(prioritySim)
            thread.start()
          }
        } else {

          // Simulate synchronously
          //
          With.simulation.step()
        }
      }

      // Skimulate
      //
      if ( ! battle.skimulationComplete && (battle.simulationComplete || With.simulation.future.isDefined)) {
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
