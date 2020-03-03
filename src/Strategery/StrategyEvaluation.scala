package Strategery

import Lifecycle.With
import Mathematics.PurpleMath
import Strategery.History.HistoricalGame
import Strategery.Strategies.Strategy

class StrategyEvaluation(val strategy: Strategy) {

  val playbookOrder       : Int                       = if (With.configuration.playbook.strategyOrder.contains(strategy)) With.configuration.playbook.strategyOrder.indexOf(strategy) else Int.MaxValue
  def gamesAll            : Iterable[HistoricalGame]  = With.strategy.gamesVsOpponent
  val gamesAllWon         : Iterable[HistoricalGame]  = gamesAll.filter(_.won)
  val gamesAllLost        : Iterable[HistoricalGame]  = gamesAll.filterNot(_.won)
  val gamesUs             : Iterable[HistoricalGame]  = gamesAll.filter(_.weEmployed(strategy))
  val gamesUsWon          : Iterable[HistoricalGame]  = gamesUs.filter(_.won)
  val target              : Double  = With.configuration.targetWinrate
  val winrateVsEnemy      : Double  = PurpleMath.nanToZero(gamesUs.map(_.winsWeighted).sum / gamesUs.map(_.weight).sum)
  val probabilityPerWin   : Double  = getProbabilityPerWin
  val probabilityPerLoss  : Double  = getProbabilityPerLoss
  val probabilityWin      : Double  = getProbabilityWin
  val probabilityLoss     : Double  = 1 - probabilityWin

  // Hyperparameter controlling how many games worth of the optimistic target winrate to assume
  // Increase to revisit lost strategies sooner
  // Decrease to abandon them more aggressively
  val priorGames = 1.5

  // Probability this strategy appears given a win
  def getProbabilityPerWin: Double = (target * priorGames + gamesAllWon.filter(_.weEmployed(strategy)).map(_.weight).sum) / (priorGames + gamesAllWon.map(_.weight).sum)

  // Probability this strategy appears given a loss
  def getProbabilityPerLoss: Double = ((1.0 - target) * priorGames + gamesAllLost.filter(_.weEmployed(strategy)).map(_.weight).sum) / (priorGames + gamesAllLost.map(_.weight).sum)

  // Probability we win given this strategy
  def getProbabilityWin: Double = (priorGames * target + gamesUsWon.map(_.weight).sum) / (priorGames + gamesUs.map(_.weight).sum)
}

