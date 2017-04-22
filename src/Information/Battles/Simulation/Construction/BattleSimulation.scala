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
  
  val doLog = With.configuration.visualize && With.configuration.visualizeSimulation
  val events = new ArrayBuffer[BattleSimulationEvent]
  
  override def toString =
    "BattleSimulation: +" +
    enemy.lostValue +
    " -" +
    us.lostValue +
    ": " +
    us.tactics
    
  
}