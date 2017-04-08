package Information.Battles.Simulation.Construction

import Information.Battles.Simulation.BattleSimulator
import Information.Battles.Simulation.Strategies.BattleStrategyWounded.BattleStrategyWounded
import Information.Battles.Simulation.Strategies.BattleStrategyFocusAirOrGround.BattleStrategyFocusAirOrGround
import Information.Battles.Simulation.Strategies.BattleStrategyMovement.BattleStrategyMovement
import Information.Battles.Simulation.Strategies.BattleStrategyWorkers.BattleStrategyWorkers
import Information.Battles.Simulation.Strategies._
import Information.Battles.{Battle, BattleGroup}

import scala.collection.mutable.ListBuffer

object BattleSimulationBuilder {
  
  def build(battle:Battle):Iterable[BattleSimulation] = {
    
    val ourGroupStrategyVariants = buildGroupStrategies(battle.us, battle.enemy)
    
    // It'd be helpful to consider all enemy group strategies and assume they pick the best,
    // but we can't afford to run that many simulations.
    val enemyGroupStrategyVariants = List(new BattleSimulationGroup(battle.enemy, new BattleStrategy(
      BattleStrategyWounded.None,
      BattleStrategyFocusAirOrGround.Nothing,
      BattleStrategyMovement.Charge,
      BattleStrategyWorkers.Ignore)))
    
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
      strategiesMovement += BattleStrategyMovement.Charge
      strategiesMovement += BattleStrategyMovement.Flee
      
      if (thatCanMove) {
        strategiesMovement += BattleStrategyMovement.Kite
      }
    }
    if (strategiesMovement.isEmpty) {
      strategiesMovement += BattleStrategyMovement.Dont
    }
    
    strategiesFleeWounded += BattleStrategyWounded.None
    if (thisCanMove)
      strategiesFleeWounded += BattleStrategyWounded.Flee
    if (thisCanMove && thisGroup.units.exists(_.melee) && thisGroup.units.exists(! _.melee))
      strategiesFleeWounded += BattleStrategyWounded.FleeRanged
    
    strategiesFocusAirOrGround += BattleStrategyFocusAirOrGround.Nothing
    if (thatGroup.units.exists(_.flying) && thatGroup.units.exists( ! _.flying)) {
      strategiesFocusAirOrGround += BattleStrategyFocusAirOrGround.Air
      strategiesFocusAirOrGround += BattleStrategyFocusAirOrGround.Ground
    }
    
    val workerCount = thisGroup.units.count(_.unitClass.worker)
    strategiesWorkersFighting += BattleStrategyWorkers.Ignore
    if (workerCount > 0) {
      strategiesWorkersFighting += BattleStrategyWorkers.AllFight
      strategiesWorkersFighting += BattleStrategyWorkers.Flee
    }
    if (workerCount > 3)
      strategiesWorkersFighting += BattleStrategyWorkers.HalfFight
    
    val strategyPermutations =
      strategiesFleeWounded.flatten(strategyFleeWounded =>
        strategiesFocusAirOrGround.flatten(strategyFocusAirOrGround =>
          strategiesMovement.flatten(strategyMovement =>
            strategiesWorkersFighting.map(strategyWorkersFighting => new BattleStrategy(
              strategyFleeWounded,
              strategyFocusAirOrGround,
              strategyMovement,
              strategyWorkersFighting
            )))))
    
    strategyPermutations.map(strategy => new BattleSimulationGroup(thisGroup, strategy))
  }
  
  def trimEnemyStrategies(variants:Iterable[BattleSimulationGroup]):Iterable[BattleSimulationGroup] = {
    variants.filter(variant =>
      variant.strategy.workersFighting == BattleStrategyWorkers.Ignore &&
      variant.strategy.focusAirOrGround == BattleStrategyFocusAirOrGround.Nothing &&
      variant.strategy.fleeWounded == BattleStrategyWounded.None)
  }
  
  def buildSimulation(ourGroup:BattleSimulationGroup, enemyGroup:BattleSimulationGroup):BattleSimulation = {
    instructWorkers(ourGroup)
    instructWorkers(enemyGroup)
    new BattleSimulation(ourGroup, enemyGroup)
  }
  
  def instructWorkers(group:BattleSimulationGroup) {
    var workersNotMining = 0
    val workers = group.units.filter(_.unit.unitClass.worker)
    
    if (group.strategy.workersFighting == BattleStrategyWorkers.Flee) {
      workersNotMining = workers.size
      workers.foreach(worker => { worker.fleeing = true; worker.fighting = false })
    }
    else if (group.strategy.workersFighting == BattleStrategyWorkers.Ignore) {
      workers.toList.foreach(_.fighting = false)
    }
    else if (group.strategy.workersFighting == BattleStrategyWorkers.HalfFight) {
      workersNotMining = workers.size / 2
      workers.toList.sortBy(-_.totalLife).take(workersNotMining).foreach(_.fighting = false)
    }
    else {
      workersNotMining = workers.size
    }
    
    group.lostValuePerSecond += BattleSimulator.costPerSecondOfNotMining(workersNotMining)
  }
}
