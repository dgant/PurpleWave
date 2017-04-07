package Information.Battles.Simulation.Construction

import Information.Battles.Simulation.Strategies.BattleStrategyFleeWounded.BattleStrategyFleeWounded
import Information.Battles.Simulation.Strategies.BattleStrategyFocusAirOrGround.BattleStrategyFocusAirOrGround
import Information.Battles.Simulation.Strategies.BattleStrategyMovement.BattleStrategyMovement
import Information.Battles.Simulation.Strategies.BattleStrategyWorkers.BattleStrategyWorkers
import Information.Battles.Simulation.Strategies._
import Information.Battles.{Battle, BattleGroup}

import scala.collection.mutable.ListBuffer

object BattleSimulationBuilder {
  
  def build(battle:Battle):Iterable[BattleSimulation] = {
    val ourGroupStrategyVariants = buildGroupStrategies(battle.us, battle.enemy)
    val enemyGroupStrategyVariants = buildGroupStrategies(battle.enemy, battle.us)
    
    ourGroupStrategyVariants.flatten(ourGroupStrategyVariant =>
      enemyGroupStrategyVariants.map(enemyGroupStrategyVariant =>
        new BattleSimulation(
          ourGroupStrategyVariant,
          enemyGroupStrategyVariant)))
  }
  
  def buildGroupStrategies(thisGroup:BattleGroup, thatGroup:BattleGroup):Iterable[BattleSimulationGroup] = {
    val strategiesMovement          = new ListBuffer[BattleStrategyMovement]
    val strategiesFleeWounded       = new ListBuffer[BattleStrategyFleeWounded]
    val strategiesFocusAirOrGround  = new ListBuffer[BattleStrategyFocusAirOrGround]
    val strategiesWorkersFighting   = new ListBuffer[BattleStrategyWorkers]
    
    val thisCanMove = thisGroup.units.exists(_.canMove)
    val thatCanMove = thatGroup.units.exists(_.canMove)
    
    if (thisCanMove) {
      strategiesMovement :+ BattleStrategyMovement.Charge
      strategiesMovement :+ BattleStrategyMovement.Flee
      
      if (thatCanMove) {
        strategiesMovement :+ BattleStrategyMovement.Kite
      }
    }
    if (strategiesMovement.isEmpty) {
      strategiesMovement :+ BattleStrategyMovement.Dont
    }
    
    strategiesFleeWounded :+ BattleStrategyFleeWounded.None
    if (thisCanMove)
      strategiesFleeWounded :+ BattleStrategyFleeWounded.Any
    if (thisCanMove && thisGroup.units.exists(_.melee) && thisGroup.units.exists(! _.melee))
      strategiesFleeWounded :+ BattleStrategyFleeWounded.Ranged
    
    strategiesFocusAirOrGround :+ BattleStrategyFocusAirOrGround.FocusNeitherAirNorGround
    if (thatGroup.units.exists(_.flying) && thatGroup.units.exists( ! _.flying)) {
      strategiesFocusAirOrGround :+ BattleStrategyFocusAirOrGround.FocusAir
      strategiesFocusAirOrGround :+ BattleStrategyFocusAirOrGround.FocusGround
    }
    
    val workerCount = thisGroup.units.count(_.unitClass.worker)
    strategiesWorkersFighting :+ BattleStrategyWorkers.None
    if (workerCount > 0) {
      strategiesWorkersFighting :+ BattleStrategyWorkers.AllFight
      strategiesWorkersFighting :+ BattleStrategyWorkers.Flee
    }
    if (workerCount > 3)
      strategiesWorkersFighting :+ BattleStrategyWorkers.HalfFight
    
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
}
