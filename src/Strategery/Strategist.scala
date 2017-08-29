package Strategery

import Lifecycle.With
import Planning.Plan
import Planning.Plans.WinTheGame
import Strategery.Strategies.Protoss.ProtossChoices
import Strategery.Strategies.Strategy
import Strategery.Strategies.Terran.TerranChoices
import Strategery.Strategies.Zerg.ZergChoices

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Strategist {
  
  lazy val selected: Set[Strategy] = selectStrategies
  
  // Plasma is so weird we need to handle it separately.
  lazy val isPlasma: Boolean = With.game.mapFileName.contains("Plasma")
  
  lazy val isIslandMap: Boolean = heyIsThisAnIslandMap
  
  lazy val gameplan: Plan = selected
    .find(_.buildGameplan().isDefined)
    .map(_.buildGameplan().get)
    .getOrElse(new WinTheGame)
  
  def selectStrategies: Set[Strategy] = {
    val strategies = filterForcedStrategies(
      (
        TerranChoices.overall ++
        ProtossChoices.overall ++
        ZergChoices.overall
      ).filter(isAppropriate))
    strategies.foreach(evaluate)
    chooseBest(strategies).toSet
  }
  
  private def filterForcedStrategies(strategies: Iterable[Strategy]): Iterable[Strategy] = {
    if (strategies.exists(Playbook.forced.contains))
      strategies.filter(Playbook.forced.contains)
    else
      strategies
  }
  
  private def isAppropriate(strategy: Strategy): Boolean = {
    val ourRace         = With.self.race
    val enemyRaces      = With.enemies.map(_.race).toSet
    val isIsland        = isIslandMap
    val isGround        = ! isIsland
    val startLocations  = With.geography.startLocations.size
    val isFfa           = With.enemies.size > 1
    
    ! Playbook.disabled.contains(strategy)            &&
    (strategy.ffa == isFfa)                           && // TODO: Disable non-ffa strategies for FFA
    (strategy.islandMaps  || ! isIsland)              &&
    (strategy.groundMaps  || ! isGround)              &&
    strategy.ourRaces.exists(_ == ourRace)            &&
    strategy.enemyRaces.exists(enemyRaces.contains)   &&
    strategy.startLocationsMin <= startLocations      &&
    strategy.startLocationsMax >= startLocations      &&
    (
      strategy.restrictedOpponents.isEmpty ||
      strategy.restrictedOpponents.get
        .map(_.toLowerCase)
        .exists(key => With.enemies.map(_.name.toLowerCase).exists(_.contains(key)))
    )
  }
  
  private def heyIsThisAnIslandMap = {
    isPlasma ||
      With.geography.startBases.forall(base1 =>
        With.geography.startBases.forall(base2 =>
          base1 == base2 || With.paths.zonePath(base1.zone, base2.zone).isEmpty))
  }

  val evaluations = new mutable.HashMap[Strategy, StrategyEvaluation]
  
  def evaluate(strategy: Strategy): StrategyEvaluation = {
    if ( ! evaluations.contains(strategy)) {
      evaluations.put(strategy, StrategyEvaluation(strategy))
      strategy.choices.flatMap(choices => filterForcedStrategies(choices.filter(isAppropriate))).foreach(evaluate)
    }
    evaluations(strategy)
  }
  
  private def chooseBest(strategies: Iterable[Strategy]): Iterable[Strategy] = {
    if (strategies.isEmpty) {
      return Iterable.empty
    }
    
    val output        = new ArrayBuffer[Strategy]
    val evaluated     = strategies.map(evaluate)
    val bestVsEnemy   = evaluated.map(_.winrateVsEnemy).max
    val untested      = evaluated.filter(_.games.isEmpty)
    
    val bestEvaluation = evaluated.maxBy(_.interestTotal)
      /*
      if (bestVsEnemy >= With.configuration.rideItOutWinrate) {
        evaluated
          .filter(_.winrateVsEnemy >= bestVsEnemy)
          .toVector
          .minBy(_.playbookOrder)
      }
      else if (untested.nonEmpty) {
        untested.minBy(_.playbookOrder)
      }
      else {
        evaluated.maxBy(_.interestTotal)
      }*/
    
    val bestStrategy = bestEvaluation.strategy
    output.append(bestStrategy)
    output ++= bestStrategy.choices.flatMap(choice => chooseBest(filterForcedStrategies(choice.filter(isAppropriate))))
    output
  }
}

