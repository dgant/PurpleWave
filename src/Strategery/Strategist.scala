package Strategery

import Lifecycle.With
import Planning.Plan
import Planning.Plans.WinTheGame
import Strategery.Choices.Overall
import Strategery.History.HistoricalGame
import Strategery.Strategies.Strategy

import scala.collection.mutable.ArrayBuffer

class Strategist {
  
  lazy val selected: Set[Strategy] = selectStrategies
  
  // Plasma is so weird we need to handle it separately.
  lazy val isPlasma: Boolean = With.game.mapFileName.contains("Plasma")
  
  lazy val isIslandMap: Boolean = heyIsThisAnIslandMap
  
  lazy val gameplan: Plan = selected
    .find(_.gameplan.isDefined)
    .map(_.gameplan.get)
    .getOrElse(new WinTheGame)
  
  def selectStrategies: Set[Strategy] = {
    val ourRace = With.self.race
    val enemyRaces = With.enemies.map(_.race).toSet
    val isIsland = isIslandMap
    val isGround = ! isIsland
    val startLocations = With.geography.startLocations.size
    
    val strategies = Overall.strategies
      .filter(strategy =>
        (strategy.islandMaps  || ! isIsland) &&
        (strategy.groundMaps  || ! isGround) &&
        strategy.ourRaces.exists(_ == ourRace) &&
        strategy.enemyRaces.exists(enemyRaces.contains) &&
        strategy.startLocationsMin <= startLocations &&
        strategy.startLocationsMax >= startLocations)
    
    chooseBest(strategies).toSet
  }
  
  private def heyIsThisAnIslandMap = {
    isPlasma ||
      With.geography.startLocations.forall(start1 =>
        With.geography.startLocations.forall(start2 =>
          !With.paths.exists(start1, start2)))
  }
  
  private val rideItOutWinrate      = 0.95
  private val goalWinrate           = 0.8 // For comparison, the #1 bot in CIG 2016 had a 65% overall winrate
  private val importanceVsEnemy     = 5.0
  private val importanceVsRace      = 3.0
  private val importanceOnMap       = 1.0
  
  private def chooseBest(strategies: Iterable[Strategy]): Iterable[Strategy] = {
    
    val output = new ArrayBuffer[Strategy]
    
    // Goal: Choose the strategy which gives us the best chance of hitting our target winrate vs. this opponent.
    // There are surely better approaches for doing this, but this is a quick and dirty solution with the CIG deadline impending.
    //
    // 1. If we have an undefeated strategy vs. this opponent, keep using it
    // 2. Otherwise, if we have untested strategies, try the one most likely to win based on prior games
    // 3. Otherwise, do some number-crunching
    
    val games              = strategies.map(strategy => (strategy, With.history.games.filter(_.strategies.contains(strategy.toString)))).toMap
    val gamesVsEnemy       = strategies.map(strategy => (strategy, games(strategy).filter(_.enemyName == With.enemy.name))).toMap
    val gamesVsRace        = strategies.map(strategy => (strategy, games(strategy).filter(_.enemyRace == With.enemy.race))).toMap
    val gamesOnMap         = strategies.map(strategy => (strategy, games(strategy).filter(_.mapName   == With.mapFileName))).toMap
    val confidenceSamples  = strategies.map(strategy => (strategy, getConfidenceSamples(strategy))).toMap
    val winratesTotal      = historyToBiasedWinrate(games)
    val winratesVsEnemy    = historyToBiasedWinrate(gamesVsEnemy)
    val winratesVsRace     = historyToBiasedWinrate(gamesVsRace)
    val winratesOnMap      = historyToBiasedWinrate(gamesOnMap)
    
    def expectedWinrate(strategy: Strategy): Double = {
      val factorEnemy   = new Factor(winratesVsEnemy(strategy), gamesVsEnemy(strategy).size,  confidenceSamples(strategy), importanceVsEnemy)
      val factorRace    = new Factor(winratesVsRace(strategy),  gamesVsRace(strategy).size,   confidenceSamples(strategy), importanceVsRace)
      val factorMap     = new Factor(winratesOnMap(strategy),   gamesOnMap(strategy).size,    confidenceSamples(strategy), importanceOnMap)
      val factors       = Vector(factorEnemy, factorRace, factorMap)
      val weighted      = weigh(factors)
      weighted
    }
    
    val biasedWinrates = strategies.map(strategy => (strategy, expectedWinrate(strategy))).toMap
    
    def chooseBestStrategy = () => {
      
      val bestVsEnemy = strategies.maxBy(winratesVsEnemy)
      lazy val untestedStrategies = strategies.filter(gamesVsEnemy(_).isEmpty)
  
      // 1. If we have a near-undefeated strategy vs. this opponent, ride it out
      if (winratesVsEnemy(bestVsEnemy) > rideItOutWinrate) {
        bestVsEnemy
      }
      // 2. If there's any strategy we haven't tried yet vs. this opponent, try the best
      else if (untestedStrategies.nonEmpty) {
        untestedStrategies.maxBy(biasedWinrates(_))
      }
      // 3. Otherwise, take the best strategy period
      else {
        strategies.maxBy(biasedWinrates)
      }
    }
    val bestStrategy = chooseBestStrategy()
    output.append(bestStrategy)
    if (bestStrategy.options.nonEmpty) {
      output ++= chooseBest(bestStrategy.options)
    }
    output
  }
  
  private class Factor(
    val winrate         : Double,
    val games           : Double,
    val confidenceGames : Double,
    val importance      : Double)
  
  private def weigh(factors: Iterable[Factor]): Double = {
    val effectiveGamesByFactor = factors.map(factor => (factor, factor.importance * Math.max(factor.games, factor.confidenceGames))).toMap
    val output  = factors.map(factor => factor.winrate * effectiveGamesByFactor(factor)).sum / effectiveGamesByFactor.values.sum
    output
  }
  
  // How many games before we have confidence in this strategy?
  // Definitely not statistically sound.
  private def getConfidenceSamples(strategy: Strategy): Double = {
    if (strategy.options.isEmpty) {
      5.0
    }
    else {
      strategy.options.map(getConfidenceSamples).sum
    }
  }
  
  private def historyToBiasedWinrate(games: Map[Strategy, Iterable[HistoricalGame]]): Map[Strategy, Double] = {
    games.keys
      .map(strategy =>
        (strategy,
        {
          // For games below confidenceSamples, assume the strategy's winrate is the target winrate
          val confidenceGames   = getConfidenceSamples(strategy)
          val gamesTotal        = games(strategy).size
          val gamesDenominator  = Math.max(gamesTotal, confidenceGames)
          val winsReal          = games(strategy).count(_.won).toDouble
          val winsNumerator     = winsReal + goalWinrate * Math.max(0, getConfidenceSamples(strategy) - gamesTotal)
          val output            = winsNumerator / gamesDenominator
          output
        }))
      .toMap
  }
}

