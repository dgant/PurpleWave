package Strategery

import Lifecycle.With
import Mathematics.PurpleMath
import Strategery.History.HistoricalGame
import Strategery.Strategies.Strategy

import scala.util.Random

case class StrategyEvaluation(strategy: Strategy) {
  
  private val importanceVsEnemy       = 100.0
  private val importanceVsRace        = 1.0
  private val importanceOnMap         = 3.0
  private val importanceWithStarts    = 3.0
  
  val playbookOrder         : Int                       = if (Playbook.strategyOrder.contains(strategy)) Playbook.strategyOrder.indexOf(strategy) else Int.MaxValue
  val games                 : Iterable[HistoricalGame]  = With.history.games.filter(_.strategies.contains(strategy.toString))
  val gamesVsEnemy          : Iterable[HistoricalGame]  = games.filter(_.enemyName      == With.enemy.name)
  val gamesVsRace           : Iterable[HistoricalGame]  = games.filter(_.enemyRace      == With.enemy.raceInitial)
  val gamesOnMap            : Iterable[HistoricalGame]  = games.filter(_.mapName        == With.mapFileName)
  val gamesWithStarts       : Iterable[HistoricalGame]  = games.filter(_.startLocations == With.game.getStartLocations.size)
  val samplesNeeded         : Double                    = getConfidenceSamples(strategy)
  val winrateTotal          : Double                    = winrate(games)
  val winrateVsEnemy        : Double                    = winrate(gamesVsEnemy)
  val winrateVsRace         : Double                    = winrate(gamesVsRace)
  val winrateOnMap          : Double                    = winrate(gamesOnMap)
  val winrateWithStarts     : Double                    = winrate(gamesWithStarts)
  val interestRaw           : Double                    = interest(games,            samplesNeeded)
  val interestVsEnemy       : Double                    = interest(gamesVsEnemy,     samplesNeeded)
  val interestVsRace        : Double                    = interest(gamesVsRace,      samplesNeeded)
  val interestOnMap         : Double                    = interest(gamesOnMap,       samplesNeeded)
  val interestWithStarts    : Double                    = interest(gamesWithStarts,  samplesNeeded)
  val interestDeterministic : Double                    = weighAllFactors
  val interestStochastic    : Double                    = Random.nextDouble()
  val interestTotal         : Double                    = With.configuration.strategyRandomness * interestStochastic + (1.0 - With.configuration.strategyRandomness) * interestDeterministic
  
  private def weighAllFactors: Double = {
    weigh(Vector(
      new WinrateFactor(interestVsEnemy,    gamesVsEnemy.size,    samplesNeeded, importanceVsEnemy),
      new WinrateFactor(interestVsRace,     gamesVsRace.size,     samplesNeeded, importanceVsRace),
      new WinrateFactor(interestOnMap,      gamesOnMap.size,      samplesNeeded, importanceOnMap),
      new WinrateFactor(interestWithStarts, gamesWithStarts.size, samplesNeeded, importanceWithStarts)))
  }
  
  private class WinrateFactor(
    val interest        : Double,
    val games           : Double,
    val confidenceGames : Double,
    val importance      : Double)
  
  private def weigh(factors: Iterable[WinrateFactor]): Double = {
    val effectiveGamesByFactor = factors.map(factor => (factor, factor.importance * Math.max(factor.games, factor.confidenceGames))).toMap
    val output = factors.map(factor => factor.interest * effectiveGamesByFactor(factor)).sum / effectiveGamesByFactor.values.sum
    output
  }
  
  // How many (decayed) games before we have confidence in this strategy?
  private def getConfidenceSamples(strategy: Strategy): Double = {
    if (strategy.choices.isEmpty) {
      1.0
    }
    else {
      Math.min(8.0, strategy.choices.map(_.map(getConfidenceSamples).sum).sum)
    }
  }
  
  private def winrate(games: Iterable[HistoricalGame]): Double = {
    PurpleMath.nanToZero(games.map(_.winsWeighted).sum / games.map(_.weight).sum)
  }
  
  private def interest(games: Iterable[HistoricalGame], confidenceSamples: Double): Double = {
    val gamesReal   = games.map(_.weight).sum
    val gamesFake   = Math.max(0.0, confidenceSamples - gamesReal)
    val winsReal    = games.map(_.winsWeighted).sum
    val winsFake    = With.configuration.targetWinrate * gamesFake
    val numerator   = winsReal + winsFake
    val denominator = gamesReal + gamesFake
    val output      = numerator / denominator
    output
  }
}