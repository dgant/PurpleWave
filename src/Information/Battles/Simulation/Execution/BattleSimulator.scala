package Information.Battles.Simulation.Execution

import Information.Battles.BattleTypes.Battle
import Information.Battles.Simulation.Construction.{BattleSimulation, BattleSimulationBuilder, BattleSimulationGroup}
import Lifecycle.With

object BattleSimulator {
  
  val maxFrames = 24 * 8
  
  //If a unit takes damage, how much do we care compared to its total value?
  val damageCostRatio = 4
  
  // Loosely based on the values in the BOSS paper:
  // http://www.aaai.org/ocs/index.php/AIIDE/AIIDE11/paper/viewFile/4078/4407
  //
  // Workers mine about 1 mineral per second.
  // We use a value of 2 minerals per second because workers lose mining time when returning from combat.
  def costPerSecondOfNotMining(workers:Int):Int = workers * 2
  
  def run() {
    With.battles.local
      .filter(_.happening)
      .foreach(battle => battle.simulations = simulate(battle))
  }
  
  def simulate(battle:Battle):Vector[BattleSimulation] = {
    val simulations = BattleSimulationBuilder.build(battle)
    simulations.foreach(runSimulation)
    simulations
  }
  
  private def runSimulation(simulation: BattleSimulation) {
    
    while(
      simulation.frameDuration < maxFrames
      && simulation.us.units.nonEmpty
      && simulation.enemy.units.nonEmpty) {
      step(simulation)
    }
    
    tallyLosses(simulation, simulation.us)
    tallyLosses(simulation, simulation.enemy)
  }
  
  private def step(battle: BattleSimulation) {
    updateAgents(battle, battle.us,     battle.enemy)
    updateAgents(battle, battle.enemy,  battle.us)
    battle.frameDuration += 1
  }
  
  private def updateAgents (
    battle    : BattleSimulation,
    thisGroup : BattleSimulationGroup,
    thatGroup : BattleSimulationGroup) {
    
    var i = 0
    while (i < thisGroup.units.size) {
      SimulacrumAgent.act(thisGroup.units(i), thisGroup, thatGroup, battle)
      i += 1
    }
  }
  
  private def tallyLosses(battle:BattleSimulation, group:BattleSimulationGroup) {
    group.lostUnits = group.units.filterNot(_.alive)
    group.lostValue =
      (group.lostValuePerSecond * battle.frameDuration) / 24 +
      group.lostUnits.map(_.unit.subjectiveValue).sum +
      group.units.map(unit => unit.unit.unitClass.subjectiveValue * unit.damageTaken / unit.unit.unitClass.maxTotalHealth).sum / damageCostRatio
  }
}
