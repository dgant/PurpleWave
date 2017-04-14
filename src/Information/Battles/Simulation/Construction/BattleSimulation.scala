package Information.Battles.Simulation.Construction

import Lifecycle.With

class BattleSimulation(
  val us    : BattleSimulationGroup,
  val enemy : BattleSimulationGroup) {
  
  var frameDuration = 0
  val frameCreated = With.frame
}