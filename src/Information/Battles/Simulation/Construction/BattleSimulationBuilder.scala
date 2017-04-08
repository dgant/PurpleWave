package Information.Battles.Simulation.Construction

import Information.Battles.Simulation.BattleSimulator
import Information.Battles.Simulation.Tactics.TacticWounded.BattleStrategyWounded
import Information.Battles.Simulation.Tactics.TacticFocusAirOrGround.BattleStrategyFocusAirOrGround
import Information.Battles.Simulation.Tactics.TacticMovement.BattleStrategyMovement
import Information.Battles.Simulation.Tactics.TacticWorkers.BattleStrategyWorkers
import Information.Battles.Simulation.Tactics._
import Information.Battles.{Battle, BattleGroup}

import scala.collection.mutable.ListBuffer

object BattleSimulationBuilder {
  
  def build(battle:Battle):Iterable[BattleSimulation] = {
    
    val ourGroupStrategyVariants = buildGroupStrategies(battle.us, battle.enemy)
    
    // It'd be helpful to consider all enemy group strategies and assume they pick the best,
    // but we can't afford to run that many simulations.
    val enemyGroupStrategyVariants = List(new BattleSimulationGroup(battle.enemy, new Tactic(
      TacticWounded.Ignore,
      TacticFocusAirOrGround.Nothing,
      TacticMovement.Charge,
      TacticWorkers.Ignore)))
    
    ourGroupStrategyVariants.flatten(ourGroupStrategyVariant =>
      enemyGroupStrategyVariants.map(enemyGroupStrategyVariant =>
        buildSimulation(
          ourGroupStrategyVariant,
          enemyGroupStrategyVariant)))
  }
  
  def buildGroupStrategies(thisGroup:BattleGroup, thatGroup:BattleGroup):Iterable[BattleSimulationGroup] = {
    val strategiesMovement          = new ListBuffer[BattleStrategyMovement]
    val strategiesFleeWounded       = new ListBuffer[BattleStrategyWounded]
    val strategiesFocusAirOrGround  = new ListBuffer[BattleStrategyFocusAirOrGround]
    val strategiesWorkersFighting   = new ListBuffer[BattleStrategyWorkers]
    
    val thisCanMove = thisGroup.units.exists(_.canMove)
    val thatCanMove = thatGroup.units.exists(_.canMove)
    
    if (thisCanMove) {
      strategiesMovement += TacticMovement.Charge
      strategiesMovement += TacticMovement.Flee
      
      if (thatCanMove) {
        strategiesMovement += TacticMovement.Kite
      }
    }
    if (strategiesMovement.isEmpty) {
      strategiesMovement += TacticMovement.Ignore
    }
    
    strategiesFleeWounded += TacticWounded.Ignore
    if (thisCanMove)
      strategiesFleeWounded += TacticWounded.Flee
    if (thisCanMove && thisGroup.units.exists(_.melee) && thisGroup.units.exists(! _.melee))
      strategiesFleeWounded += TacticWounded.FleeRanged
    
    strategiesFocusAirOrGround += TacticFocusAirOrGround.Nothing
    if (thatGroup.units.exists(_.flying) && thatGroup.units.exists( ! _.flying)) {
      strategiesFocusAirOrGround += TacticFocusAirOrGround.Air
      strategiesFocusAirOrGround += TacticFocusAirOrGround.Ground
    }
    
    val workerCount = thisGroup.units.count(_.unitClass.isWorker)
    strategiesWorkersFighting += TacticWorkers.Ignore
    if (workerCount > 0) {
      strategiesWorkersFighting += TacticWorkers.AllFight
      strategiesWorkersFighting += TacticWorkers.Flee
    }
    if (workerCount > 3)
      strategiesWorkersFighting += TacticWorkers.HalfFight
    
    val strategyPermutations =
      strategiesFleeWounded.flatten(strategyFleeWounded =>
        strategiesFocusAirOrGround.flatten(strategyFocusAirOrGround =>
          strategiesMovement.flatten(strategyMovement =>
            strategiesWorkersFighting.map(strategyWorkersFighting => new Tactic(
              strategyFleeWounded,
              strategyFocusAirOrGround,
              strategyMovement,
              strategyWorkersFighting
            )))))
    
    strategyPermutations.map(strategy => new BattleSimulationGroup(thisGroup, strategy))
  }
  
  def trimEnemyStrategies(variants:Iterable[BattleSimulationGroup]):Iterable[BattleSimulationGroup] = {
    variants.filter(variant =>
      variant.strategy.workersFighting == TacticWorkers.Ignore &&
      variant.strategy.focusAirOrGround == TacticFocusAirOrGround.Nothing &&
      variant.strategy.fleeWounded == TacticWounded.Ignore)
  }
  
  def buildSimulation(ourGroup:BattleSimulationGroup, enemyGroup:BattleSimulationGroup):BattleSimulation = {
    instructWorkers(ourGroup)
    instructWorkers(enemyGroup)
    new BattleSimulation(ourGroup, enemyGroup)
  }
  
  def instructWorkers(group:BattleSimulationGroup) {
    var workersNotMining = 0
    val workers = group.units.filter(_.unit.unitClass.isWorker)
    
    if (group.strategy.workersFighting == TacticWorkers.Flee) {
      workersNotMining = workers.size
      workers.foreach(worker => { worker.fleeing = true; worker.fighting = false })
    }
    else if (group.strategy.workersFighting == TacticWorkers.Ignore) {
      workers.toList.foreach(_.fighting = false)
    }
    else if (group.strategy.workersFighting == TacticWorkers.HalfFight) {
      workersNotMining = workers.size / 2
      workers.toList.sortBy(_.totalLife).take(workersNotMining).foreach(_.fighting = false)
    }
    else {
      workersNotMining = workers.size
    }
    
    group.lostValuePerSecond += BattleSimulator.costPerSecondOfNotMining(workersNotMining)
  }
}
