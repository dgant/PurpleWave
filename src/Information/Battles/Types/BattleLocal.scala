package Information.Battles.Types

import Information.Battles.Estimations.Estimation
import Information.Battles.Estimations.Simulation.BattleSimulation
import Lifecycle.With

class BattleLocal(us: Team, enemy: Team) extends Battle(us, enemy) {
  
  lazy val estimationSimulationAttack   : Estimation  = estimateSimulation(this, weAttack = true)
  lazy val estimationSimulationRetreat  : Estimation  = estimateSimulation(this, weAttack = false)
  
  lazy val attackGains    : Double = estimationSimulationAttack.costToEnemy
  lazy val attackLosses   : Double = estimationSimulationAttack.costToUs
  lazy val retreatGains   : Double = estimationSimulationRetreat.costToEnemy
  lazy val retreatLosses  : Double = estimationSimulationRetreat.costToUs
  lazy val desireTotal    : Double = attackGains + retreatLosses / With.configuration.retreatCaution - retreatGains - attackLosses / desireMultiplier
  
  def estimateSimulation(battle: Battle, weAttack: Boolean): Estimation = {
    val simulation = new BattleSimulation(battle, weAttack)
    simulation.run()
    simulation.estimation
  }
}
