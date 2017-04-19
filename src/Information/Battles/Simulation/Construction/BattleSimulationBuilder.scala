package Information.Battles.Simulation.Construction

import Information.Battles.Simulation.BattleSimulator
import Information.Battles.Types.Tactics.Tactic
import Information.Battles.Types.{Battle, BattleGroup, Tactics, TacticsOptions}

import scala.collection.mutable.ListBuffer

object BattleSimulationBuilder {
  
  def build(battle:Battle):Iterable[BattleSimulation] = {
    
    val ourTacticsVariants = buildOurTacticVariants(battle.us, battle.enemy)
  
    buildOurTacticVariants(battle.us, battle.enemy).flatten(ourTactics =>
      buildEnemyTacticVariants(battle.enemy, battle.us).map(enemyTactics =>
        buildSimulation(ourTactics, enemyTactics)))
  }
  
  def buildOurTacticVariants(thisGroup:BattleGroup, thatGroup:BattleGroup):Iterable[BattleSimulationGroup] = {
    val tacticsMovement = new ListBuffer[Tactic]
    val tacticsWounded  = new ListBuffer[Tactic]
    val tacticsFocus    = new ListBuffer[Tactic]
    val tacticsWorkers  = new ListBuffer[Tactic]
    
    val thisCanMove = thisGroup.units.exists(_.canMoveThisFrame)
    val thatCanMove = thatGroup.units.exists(_.canMoveThisFrame)
    
    if (thisCanMove) {
      tacticsMovement += Tactics.Movement.Charge
      tacticsMovement += Tactics.Movement.Flee
      
      if (thatCanMove) {
        tacticsMovement += Tactics.Movement.Kite
      }
    }
    if (tacticsMovement.isEmpty) {
      tacticsMovement += Tactics.Movement.None
    }

    tacticsWounded += Tactics.Wounded.Fight
    if (thisCanMove) {
      tacticsWounded += Tactics.Wounded.Flee
    }
    
    tacticsFocus += Tactics.Focus.None
    if (thatGroup.units.exists(_.flying) && thatGroup.units.exists( ! _.flying)) {
      tacticsFocus += Tactics.Focus.Air
      tacticsFocus += Tactics.Focus.Ground
    }
    
    val workerCount = thisGroup.units.count(_.unitClass.isWorker)
    tacticsWorkers += Tactics.Workers.Ignore
    if (workerCount > 0) {
      tacticsWorkers += Tactics.Workers.FightAll
      tacticsWorkers += Tactics.Workers.Flee
    }
    if (workerCount > 3) {
      tacticsWorkers += Tactics.Workers.FightHalf
    }
    
    val tacticsPermutations =
      tacticsWounded.flatten(wounded =>
        tacticsFocus.flatten(focus =>
          tacticsMovement.flatten(movement =>
            tacticsWorkers.map(workers => {
              val output = new TacticsOptions
              output.add(focus)
              output.add(movement)
              output.add(workers)
              output.add(wounded)
              output
            }))))
    
    tacticsPermutations.map(tactics => new BattleSimulationGroup(thisGroup, tactics))
  }
  
  def buildEnemyTacticVariants(thisGroup:BattleGroup, thatGroup:BattleGroup):Iterable[BattleSimulationGroup] = {
    val tactics = new TacticsOptions
    tactics.add(Tactics.Movement.Charge)
    tactics.add(Tactics.Focus.None)
    tactics.add(Tactics.Wounded.Fight)
    tactics.add(Tactics.Workers.Ignore)
    Vector(new BattleSimulationGroup(thisGroup, tactics))
  }
  
  def buildSimulation(ourGroup:BattleSimulationGroup, enemyGroup:BattleSimulationGroup):BattleSimulation = {
    instructWorkers(ourGroup)
    instructWorkers(enemyGroup)
    new BattleSimulation(ourGroup, enemyGroup)
  }
  
  def instructWorkers(group:BattleSimulationGroup) {
    var workersNotMining = 0
    val workers = group.units.filter(_.unit.unitClass.isWorker)
    
    if (group.tactics.has(Tactics.Workers.Flee)) {
      workersNotMining = workers.size
      workers.foreach(worker => { worker.fleeing = true; worker.fighting = false })
    }
    else if (group.tactics.has(Tactics.Workers.Ignore)) {
      workers.toVector.foreach(_.fighting = false)
    }
    else if (group.tactics.has(Tactics.Workers.FightHalf)) {
      workersNotMining = workers.size / 2
      workers.toVector.sortBy(_.totalLife).take(workersNotMining).foreach(_.fighting = false)
    }
    else if (group.tactics.has(Tactics.Workers.FightAll)) {
      workersNotMining = workers.size
    }
    
    group.lostValuePerSecond += BattleSimulator.costPerSecondOfNotMining(workersNotMining)
  }
}
