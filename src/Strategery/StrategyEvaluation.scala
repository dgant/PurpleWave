package Strategery

import Lifecycle.With
import Strategery.History.HistoricalGame
import Strategery.Strategies.Strategy

case class StrategyEvaluation(strategy: Strategy) {
  private val goalWinrate           = 0.8 // For comparison, the #1 bot in CIG 2016 had a 65% overall winrate
  private val importanceVsEnemy     = 5.0
  private val importanceVsRace      = 3.0
  private val importanceOnMap       = 1.0
  
  val games             : Iterable[HistoricalGame]  = With.history.games.filter(_.strategies.contains(strategy.toString))
  val gamesVsEnemy      : Iterable[HistoricalGame]  = games.filter(_.enemyName == With.enemy.name)
  val gamesVsRace       : Iterable[HistoricalGame]  = games.filter(_.enemyRace == With.enemy.race)
  val gamesOnMap        : Iterable[HistoricalGame]  = games.filter(_.mapName   == With.mapFileName)
  val samplesNeeded     : Double                    = getConfidenceSamples(strategy)
  val winrateTotal      : Double                    = winrate(games)
  val winrateVsEnemy    : Double                    = winrate(gamesVsEnemy)
  val winrateVsRace     : Double                    = winrate(gamesVsRace)
  val winrateOnMap      : Double                    = winrate(gamesOnMap)
  val interestRaw       : Double                    = optimisticWinrate(games,        samplesNeeded)
  val interestVsEnemy   : Double                    = optimisticWinrate(gamesVsEnemy, samplesNeeded)
  val interestVsRace    : Double                    = optimisticWinrate(gamesVsRace,  samplesNeeded)
  val interestOnMap     : Double                    = optimisticWinrate(gamesOnMap,   samplesNeeded)
  val interestTotal: Double = weigh(Vector(
    new WinrateFactor(winrateVsEnemy, gamesVsEnemy.size,  samplesNeeded, importanceVsEnemy),
    new WinrateFactor(winrateVsRace,  gamesVsRace.size,   samplesNeeded, importanceVsRace),
    new WinrateFactor(winrateOnMap,   gamesOnMap.size,    samplesNeeded, importanceOnMap)))
  
  private class WinrateFactor(
    val winrate         : Double,
    val games           : Double,
    val confidenceGames : Double,
    val importance      : Double)
  
  private def weigh(factors: Iterable[WinrateFactor]): Double = {
    val effectiveGamesByFactor = factors.map(factor => (factor, factor.importance * Math.max(factor.games, factor.confidenceGames))).toMap
    val output  = factors.map(factor => factor.winrate * effectiveGamesByFactor(factor)).sum / effectiveGamesByFactor.values.sum
    output
  }
  
  // How many games before we have confidence in this strategy?
  // Definitely not statistically sound.
  private def getConfidenceSamples(strategy: Strategy): Double = {
    if (strategy.choices.isEmpty) {
      3.0
    }
    else {
      strategy.choices.map(_.map(getConfidenceSamples).sum).sum
    }
  }
  
  private def winrate(games: Iterable[HistoricalGame]): Double = {
    if (games.isEmpty) {
      0.0
    }
    else {
      games.count(_.won).toDouble / games.size
    }
  }
  
  private def optimisticWinrate(games: Iterable[HistoricalGame], confidenceSamples: Double): Double = {
    val wins        = games.count(_.won).toDouble
    val numerator   = wins + With.configuration.rideItOutWinrate * Math.max(0, confidenceSamples - games.size)
    val denominator = Math.max(games.size, confidenceSamples)
    numerator / denominator
  }
}