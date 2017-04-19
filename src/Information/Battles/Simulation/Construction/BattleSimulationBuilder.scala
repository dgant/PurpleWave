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
      tacticsMovement += Tactics.MovementCharge
      tacticsMovement += Tactics.MovementFlee
      
      if (thatCanMove) {
        tacticsMovement += Tactics.MovementKite
      }
    }
    if (tacticsMovement.isEmpty) {
      tacticsMovement += Tactics.MovementNone
    }

    tacticsWounded += Tactics.WoundedFight
    if (thisCanMove) {
      tacticsWounded += Tactics.WoundedFlee
    }
    
    tacticsFocus += Tactics.FocusNone
    if (thatGroup.units.exists(_.flying) && thatGroup.units.exists( ! _.flying)) {
      tacticsFocus += Tactics.FocusAir
      tacticsFocus += Tactics.FocusGround
    }
    
    val workerCount = thisGroup.units.count(_.unitClass.isWorker)
    tacticsWorkers += Tactics.WorkersIgnore
    if (workerCount > 0) {
      tacticsWorkers += Tactics.WorkersFightAll
      tacticsWorkers += Tactics.WorkersFlee
    }
    if (workerCount > 3) {
      tacticsWorkers += Tactics.WorkersFightHalf
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
    tactics.add(Tactics.MovementCharge)
    tactics.add(Tactics.FocusNone)
    tactics.add(Tactics.WoundedFight)
    tactics.add(Tactics.WorkersIgnore)
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
    
    if (group.tactics.has(Tactics.WorkersFlee)) {
      workersNotMining = workers.size
      workers.foreach(worker => { worker.fleeing = true; worker.fighting = false })
    }
    else if (group.tactics.has(Tactics.WorkersIgnore)) {
      workers.toVector.foreach(_.fighting = false)
    }
    else if (group.tactics.has(Tactics.WorkersFightHalf)) {
      workersNotMining = workers.size / 2
      workers.toVector.sortBy(_.totalLife).take(workersNotMining).foreach(_.fighting = false)
    }
    else if (group.tactics.has(Tactics.WorkersFightAll)) {
      workersNotMining = workers.size
    }
    
    group.lostValuePerSecond += BattleSimulator.costPerSecondOfNotMining(workersNotMining)
  }
}
