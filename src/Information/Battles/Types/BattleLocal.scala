package Information.Battles.Types

import Information.Battles.Prediction.PredictionLocal
import Information.Battles.Prediction.Simulation.Simulation
import Lifecycle.With

class BattleLocal(us: Team, enemy: Team) extends Battle(us, enemy) {

  var estimationSimulationAttack: Option[PredictionLocal] = None
  var estimationSimulationSnipe: Option[PredictionLocal] = None

  var judgement: Option[BattleJudgment] = None

  def estimate(): Unit = {
    estimationSimulationAttack = estimationSimulationAttack.orElse(Some(estimateSimulation(this, weAttack = true, weSnipe = false)))
    estimationSimulationSnipe = estimationSimulationSnipe.orElse(
      if (With.self.isZerg)
        Some(estimateSimulation(this, weAttack = true, weSnipe = true))
      else
        estimationSimulationAttack)
  }

  private def estimateSimulation(battle: BattleLocal, weAttack: Boolean, weSnipe: Boolean): PredictionLocal = {
    val simulation = new Simulation(battle, weAttack, weSnipe)
    simulation.run()
    simulation.estimation
  }
}
