package Information.Battles.Simulator

import Information.Battles.Battle
import Utilities.EnrichPosition._

object BattleSimulator {

  val chargingPenalty = 0.6 //What fraction of top speed units are likely to get
  val movementFrames = 4
  val maxFrames = 24 * 10
  
  def simulate(battle:Battle):BattleSimulation = {
    val simulation = new BattleSimulation(battle)
    
    for(frame <- 0 until maxFrames) {
      step(simulation)
    }
  
    simulation
  }
  
  private def step(battle: BattleSimulation) {
    attackOrMove(battle, battle.ourUnits, battle.enemyUnits)
    attackOrMove(battle, battle.enemyUnits, battle.ourUnits)
    removeDeadUnits(battle)
    reduceCooldown(battle.ourUnits)
    reduceCooldown(battle.enemyUnits)
  }
  
  private def attackOrMove(
    battle    : BattleSimulation,
    actives   : Iterable[Simulacrum],
    passives  : Iterable[Simulacrum]) {
    actives
      .filter(active => active.attackCooldown == 0 || active.moveCooldown == 0)
      .foreach(active => {
        val targets = passives.filter(passive =>
          active.unit.canAttackThisSecond(passive.unit))
        val targetsInRange = targets.filter(passive =>
          active.unit.rangeAgainst(passive.unit) <= active.pixel.getDistance(passive.pixel))
        
        if (targetsInRange.nonEmpty) {
          val target = targetsInRange.minBy(_.totalLife)
          val damage = active.unit.damageAgainst(target.unit, target.shields)
          active.attackCooldown = active.unit.cooldownAgainst(target.unit)
          active.moveCooldown = Math.min(active.attackCooldown, 8)
          target.shields -= damage
          if (target.shields < 0) {
            target.hitPoints += target.shields
            target.shields = 0
          }
        }
        else if (targets.nonEmpty) {
          val target = targets.minBy(_.pixel.getDistance(active.pixel))
          active.pixel = active.pixel.project(target.pixel, active.unit.topSpeed * chargingPenalty * (1 + movementFrames))
          active.attackCooldown = movementFrames
          active.moveCooldown = movementFrames
        }
      })
  }
  
  private def removeDeadUnits(battle:BattleSimulation) {
    battle.ourUnits.filterNot(_.alive).foreach(deadUnit => {
      battle.ourLostValue += value(deadUnit)
      battle.ourUnits.remove(deadUnit)
    })
    battle.enemyUnits.filterNot(_.alive).foreach(deadUnit => {
      battle.enemyLostValue += value(deadUnit)
      battle.enemyUnits.remove(deadUnit)
    })
  }
  
  private def value(unit:Simulacrum):Int = {
    2 * unit.unit.unitClass.mineralPrice +
    3 * unit.unit.unitClass.gasPrice +
    (if (unit.unit.unitClass.isWorker) 50 else 0)
  }
  
  private def reduceCooldown(simulacra:Iterable[Simulacrum]) {
    simulacra.foreach(simulacrum => {
      simulacrum.attackCooldown = Math.max(0, simulacrum.attackCooldown - 1)
      simulacrum.moveCooldown   = Math.max(0, simulacrum.moveCooldown   - 1)
    })
  }
}
