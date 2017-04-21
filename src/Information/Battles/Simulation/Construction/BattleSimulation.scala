package Information.Battles.Simulation.Construction

import Information.Battles.TacticsTypes.TacticsOptions
import Lifecycle.With

import scala.collection.mutable.ArrayBuffer

class BattleSimulation(
  val us    : BattleSimulationGroup,
  val enemy : BattleSimulationGroup) {
  
  //Intended to get fancier by min-maxing against enemy tactics
  def tactics:TacticsOptions = us.tactics
  
  var frameDuration = 0
  val frameCreated = With.frame
  
  val doLog = With.configuration.visualizeSimulation
  val events = new ArrayBuffer[BattleSimulationEvent]
}