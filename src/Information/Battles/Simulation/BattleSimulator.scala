package Information.Battles.Simulation

import Information.Battles.Battle
import Utilities.EnrichPosition._

object BattleSimulator {

  val chargingPenalty = 0.6 //What fraction of top speed units are likely to get
  val movementFrames = 4
  val maxFrames = 24 * 10
  
  def simulate(battle:Battle):Iterable[BattleSimulation] = {
    val simulations = BattleSimulationBuilder.build(battle)
    simulations.foreach(runSimulation)
    simulations
  }
  
  private def runSimulation(simulation: BattleSimulation) {
    for(frame <- 0 until maxFrames) step(simulation)
  }
  
  private def step(battle: BattleSimulation) {
    attackOrMove(battle, battle.us,     battle.enemy)
    attackOrMove(battle, battle.enemy,  battle.us)
    removeDeadUnits(battle.us)
    removeDeadUnits(battle.enemy)
    reduceCooldown(battle.us)
    reduceCooldown(battle.enemy)
  }
  
  private def attackOrMove(
    battle    : BattleSimulation,
    attackers : BattleSimulationGroup,
    defenders : BattleSimulationGroup) {
    attackers.units
      .filter(attacker => attacker.attackCooldown == 0 || attacker.moveCooldown == 0)
      .foreach(attacker => {
        val targets = defenders.units.filter(defender =>
          attacker.unit.canAttackThisSecond(defender.unit))
        val targetsInRange = targets.filter(passive =>
          attacker.unit.rangeAgainst(passive.unit) >= attacker.pixel.getDistance(passive.pixel))
        
        if (targetsInRange.nonEmpty) {
          val target = targetsInRange.minBy(_.totalLife)
          val damage = attacker.unit.damageAgainst(target.unit, target.shields)
          attacker.attackCooldown = attacker.unit.cooldownAgainst(target.unit)
          attacker.moveCooldown = Math.min(attacker.attackCooldown, 8)
          target.shields -= damage
          if (target.shields < 0) {
            target.hitPoints += target.shields
            target.shields = 0
          }
        }
        else if (targets.nonEmpty) {
          val target = targets.minBy(_.pixel.getDistance(attacker.pixel))
          attacker.pixel = attacker.pixel.project(target.pixel, attacker.unit.topSpeed * chargingPenalty * (1 + movementFrames))
          attacker.attackCooldown = movementFrames
          attacker.moveCooldown = movementFrames
        }
      })
  }
  
  private def removeDeadUnits(group:BattleSimulationGroup) {
    group.units.filterNot(_.alive).foreach(deadUnit => {
      group.lostValue += value(deadUnit)
      group.units.remove(deadUnit)
    })
  }
  
  private def value(unit:Simulacrum):Int = {
    2 * unit.unit.unitClass.mineralPrice +
    3 * unit.unit.unitClass.gasPrice +
    (if (unit.unit.unitClass.worker) 50 else 0)
  }
  
  private def reduceCooldown(group:BattleSimulationGroup) {
    group.units.foreach(simulacrum => {
      simulacrum.attackCooldown = Math.max(0, simulacrum.attackCooldown - 1)
      simulacrum.moveCooldown   = Math.max(0, simulacrum.moveCooldown   - 1)
    })
  }
}
