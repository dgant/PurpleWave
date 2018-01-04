package Information.Battles.Types

import Information.Battles.Prediction.Prediction
import Information.Battles.Prediction.Simulation.Simulation
import Lifecycle.With

class BattleLocal(us: Team, enemy: Team) extends Battle(us, enemy) {
  
  lazy val estimationSimulationAttack   : Prediction  = estimateSimulation(this, weAttack = true)
  lazy val estimationSimulationRetreat  : Prediction  = estimateSimulation(this, weAttack = false)
  
  lazy val attackGains    : Double = estimationSimulationAttack.costToEnemy
  lazy val attackLosses   : Double = estimationSimulationAttack.costToUs
  lazy val retreatGains   : Double = estimationSimulationRetreat.costToEnemy
  lazy val retreatLosses  : Double = estimationSimulationRetreat.costToUs
  lazy val netEngageValue : Double = With.blackboard.aggressionRatio * attackGains + retreatLosses - retreatGains - attackLosses
  
  def estimateSimulation(battle: BattleLocal, weAttack: Boolean): Prediction = {
    val simulation = new Simulation(battle, weAttack)
    simulation.run()
    simulation.estimation
  }
}
