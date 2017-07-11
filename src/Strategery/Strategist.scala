package Strategery

import Lifecycle.With
import Planning.Plan
import Planning.Plans.WinTheGame
import Strategery.Strategies.Options.Protoss.ProtossChoices
import Strategery.Strategies.Strategy

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
    val strategies = ProtossChoices.options.filter(isAppropriate)
    strategies.foreach(evaluate)
    chooseBest(strategies).toSet
  }
  
  private def isAppropriate(strategy: Strategy): Boolean = {
    val ourRace = With.self.race
    val enemyRaces = With.enemies.map(_.race).toSet
    val isIsland = isIslandMap
    val isGround = ! isIsland
    val startLocations = With.geography.startLocations.size
    
    (strategy.islandMaps  || ! isIsland)            &&
    (strategy.groundMaps  || ! isGround)            &&
    strategy.ourRaces.exists(_ == ourRace)          &&
    strategy.enemyRaces.exists(enemyRaces.contains) &&
    strategy.startLocationsMin <= startLocations    &&
    strategy.startLocationsMax >= startLocations
  }
  
  private def heyIsThisAnIslandMap = {
    isPlasma ||
      With.geography.startLocations.forall(start1 =>
        With.geography.startLocations.forall(start2 =>
          !With.paths.exists(start1, start2)))
  }

  val evaluations = new mutable.HashMap[Strategy, StrategyEvaluation]
  
  def evaluate(strategy: Strategy): StrategyEvaluation = {
    if ( ! evaluations.contains(strategy)) {
      evaluations.put(strategy, StrategyEvaluation(strategy))
      strategy.choices.flatten.filter(isAppropriate).foreach(evaluate)
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
    
    val bestEvaluation =
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
      }
    
    val bestStrategy = bestEvaluation.strategy
    output.append(bestStrategy)
    output ++= bestStrategy.choices.flatMap(choice => chooseBest(choice.filter(isAppropriate)))
    output
  }
}

