package Information.Battles.Simulation.Construction

import Information.Battles.TacticsTypes.TacticsOptions
import Lifecycle.With

class BattleSimulation(
  val us    : BattleSimulationGroup,
  val enemy : BattleSimulationGroup) {
  
  //Intended to get fancier by min-maxing against enemy tactics
  def tactics:TacticsOptions = us.tactics
  
  var frameDuration = 0
  val frameCreated = With.frame
}