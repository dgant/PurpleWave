package Information.Battles.Simulation.Construction

import Information.Battles.Simulation.BattleSimulator
import Information.Battles.Simulation.Tactics.TacticWounded.TacticWounded
import Information.Battles.Simulation.Tactics.TacticFocus.TacticFocus
import Information.Battles.Simulation.Tactics.TacticMovement.TacticMovement
import Information.Battles.Simulation.Tactics.TacticWorkers.TacticWorkers
import Information.Battles.Simulation.Tactics._
import Information.Battles.{Battle, BattleGroup}

import scala.collection.mutable.ListBuffer

object BattleSimulationBuilder {
  
  def build(battle:Battle):Iterable[BattleSimulation] = {
    
    val ourTacticsVariants = buildOurTacticVariants(battle.us, battle.enemy)
  
    buildOurTacticVariants(battle.us, battle.enemy).flatten(ourTactics =>
      buildEnemyTacticVariants(battle.enemy, battle.us).map(enemyTactics =>
        buildSimulation(ourTactics, enemyTactics)))
  }
  
  def buildOurTacticVariants(thisGroup:BattleGroup, thatGroup:BattleGroup):Iterable[BattleSimulationGroup] = {
    val tacticsMovement          = new ListBuffer[TacticMovement]
    val tacticsFleeWounded       = new ListBuffer[TacticWounded]
    val tacticsFocusAirOrGround  = new ListBuffer[TacticFocus]
    val tacticsWorkersFighting   = new ListBuffer[TacticWorkers]
    
    val thisCanMove = thisGroup.units.exists(_.canMove)
    val thatCanMove = thatGroup.units.exists(_.canMove)
    
    if (thisCanMove) {
      tacticsMovement += TacticMovement.Charge
      tacticsMovement += TacticMovement.Flee
      
      if (thatCanMove) {
        tacticsMovement += TacticMovement.Kite
      }
    }
    if (tacticsMovement.isEmpty) {
      tacticsMovement += TacticMovement.Ignore
    }
    
    /*
    tacticsFleeWounded += TacticWounded.Ignore
    if (thisCanMove)
      tacticsFleeWounded += TacticWounded.Flee
      */
    tacticsFleeWounded += TacticWounded.Flee
    
    /*
    if (thisCanMove && thisGroup.units.exists(_.melee) && thisGroup.units.exists(! _.melee))
      tacticsFleeWounded += TacticWounded.FleeRanged
      */
    
    tacticsFocusAirOrGround += TacticFocus.Nothing
    /*
    if (thatGroup.units.exists(_.flying) && thatGroup.units.exists( ! _.flying)) {
      tacticsFocusAirOrGround += TacticFocus.Air
      tacticsFocusAirOrGround += TacticFocus.Ground
    }
    */
    
    val workerCount = thisGroup.units.count(_.unitClass.isWorker)
    tacticsWorkersFighting += TacticWorkers.Ignore
    /*
    if (workerCount > 0) {
      tacticsWorkersFighting += TacticWorkers.AllFight
      tacticsWorkersFighting += TacticWorkers.Flee
    }
    if (workerCount > 3)
      tacticsWorkersFighting += TacticWorkers.HalfFight
    */
    
    val strategyPermutations =
      tacticsFleeWounded.flatten(strategyFleeWounded =>
        tacticsFocusAirOrGround.flatten(strategyFocusAirOrGround =>
          tacticsMovement.flatten(strategyMovement =>
            tacticsWorkersFighting.map(strategyWorkersFighting => new Tactics(
              strategyFleeWounded,
              strategyFocusAirOrGround,
              strategyMovement,
              strategyWorkersFighting
            )))))
    
    strategyPermutations.map(strategy => new BattleSimulationGroup(thisGroup, strategy))
  }
  
  def buildEnemyTacticVariants(thisGroup:BattleGroup, thatGroup:BattleGroup):Iterable[BattleSimulationGroup] = {
    List(new BattleSimulationGroup(thisGroup, new Tactics(
      TacticWounded.Ignore,
      TacticFocus.Nothing,
      TacticMovement.Charge,
      TacticWorkers.Ignore)))
  }
  
  def trimEnemytactics(variants:Iterable[BattleSimulationGroup]):Iterable[BattleSimulationGroup] = {
    variants.filter(variant =>
      variant.tactics.workers == TacticWorkers.Ignore &&
      variant.tactics.focusAirOrGround == TacticFocus.Nothing &&
      variant.tactics.wounded == TacticWounded.Ignore)
  }
  
  def buildSimulation(ourGroup:BattleSimulationGroup, enemyGroup:BattleSimulationGroup):BattleSimulation = {
    instructWorkers(ourGroup)
    instructWorkers(enemyGroup)
    new BattleSimulation(ourGroup, enemyGroup)
  }
  
  def instructWorkers(group:BattleSimulationGroup) {
    var workersNotMining = 0
    val workers = group.units.filter(_.unit.unitClass.isWorker)
    
    if (group.tactics.workers == TacticWorkers.Flee) {
      workersNotMining = workers.size
      workers.foreach(worker => { worker.fleeing = true; worker.fighting = false })
    }
    else if (group.tactics.workers == TacticWorkers.Ignore) {
      workers.toList.foreach(_.fighting = false)
    }
    else if (group.tactics.workers == TacticWorkers.HalfFight) {
      workersNotMining = workers.size / 2
      workers.toList.sortBy(_.totalLife).take(workersNotMining).foreach(_.fighting = false)
    }
    else {
      workersNotMining = workers.size
    }
    
    group.lostValuePerSecond += BattleSimulator.costPerSecondOfNotMining(workersNotMining)
  }
}
