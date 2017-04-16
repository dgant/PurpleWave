package Information.Battles.Simulation

import Information.Battles.Simulation.Construction.{BattleSimulation, BattleSimulationBuilder, BattleSimulationGroup, Simulacrum}
import Information.Battles.Types.Battle
import Lifecycle.With

object BattleSimulator {
  
  val maxFrames = 24 * 5
  
  //If a unit takes damage, how much do we care compared to its total value?
  val damageCostRatio = 3
  
  // Loosely based on the values in the BOSS paper:
  // http://www.aaai.org/ocs/index.php/AIIDE/AIIDE11/paper/viewFile/4078/4407
  //
  // Workers mine about 1 mineral per second.
  // We use a value of 2 minerals per second because workers lose mining time when returning from combat.
  def costPerSecondOfNotMining(workers:Int):Int = workers * 2
  
  def run() {
    With.battles.all
      .filter(_.happening)
      .foreach(battle => battle.simulations = simulate(battle))
  }
  
  def simulate(battle:Battle):Iterable[BattleSimulation] = {
    val simulations = BattleSimulationBuilder.build(battle)
    simulations.foreach(runSimulation)
    simulations
  }
  
  private def runSimulation(simulation: BattleSimulation) {
    while(simulation.frameDuration < maxFrames && simulation.us.units.nonEmpty && simulation.enemy.units.nonEmpty)
      step(simulation)
    tallyLosses(simulation, simulation.us)
    tallyLosses(simulation, simulation.enemy)
  }
  
  private def step(battle: BattleSimulation) {
    updateAgents    (battle, battle.us,     battle.enemy)
    updateAgents    (battle, battle.enemy,  battle.us)
    reduceCooldown  (battle.us)
    reduceCooldown  (battle.enemy)
    battle.frameDuration += 1
  }
  
  private def updateAgents (
    battle    : BattleSimulation,
    thisGroup : BattleSimulationGroup,
    thatGroup : BattleSimulationGroup) {
    thisGroup.units
      .foreach(thisUnit =>
        if (thisUnit.alive && (thisUnit.readyToAttack || thisUnit.readyToMove))
        new SimulacrumAgent(thisUnit, thisGroup, thatGroup, battle).act)
  }
  
  private def value(unit:Simulacrum):Int = {
    (if (unit.unit.unitClass.isWorker) 2 else 1) *
    (
      2 * unit.unit.unitClass.mineralValue +
      3 * unit.unit.unitClass.gasValue
    )
  }
  
  private def reduceCooldown(group:BattleSimulationGroup) {
    group.units.foreach(simulacrum => {
      simulacrum.attackCooldown = Math.max(0, simulacrum.attackCooldown - 1)
      simulacrum.moveCooldown   = Math.max(0, simulacrum.moveCooldown   - 1)
    })
  }
  
  private def tallyLosses(battle:BattleSimulation, group:BattleSimulationGroup) {
    group.lostUnits = group.units.filterNot(_.alive)
    group.lostValue =
      (group.lostValuePerSecond * battle.frameDuration) / 24 +
      group.lostUnits.map(value).sum +
      group.units.map(unit => value(unit) * unit.damageTaken / unit.unit.unitClass.maxTotalHealth).sum / damageCostRatio
  }
}
