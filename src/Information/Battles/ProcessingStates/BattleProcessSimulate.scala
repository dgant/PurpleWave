package Information.Battles.ProcessingStates

import Information.Battles.Prediction.PredictionLocal
import Information.Battles.Prediction.Simulation.Simulation
import Information.Battles.Types.{Battle, BattleLocal}
import Lifecycle.With

class BattleProcessSimulate extends BattleProcessState {
  class BattleSimulation(val battle: BattleLocal) {
    var simulationAttack: Option[Simulation] = None
    var simulationSnipe: Option[Simulation] = None

    var predictionAttack: Option[PredictionLocal] = None
    var predictionSnipe: Option[PredictionLocal] = None
  }

  var battles: Seq[Battle] = _

  override def step(): Unit = {
    val battles = With.battles.nextBattlesLocal

    val unsimulated = battles.view.flatMap(b => b.predictions.map(p => (b, p))).find( ! _._2.simulation.complete)
    if (unsimulated.isDefined) {
      unsimulated.get._2.simulation.step()
      return
    }

    transitionTo(new BattleProcessJudge)
  }
}
