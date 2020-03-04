package Strategery

import Lifecycle.With
import Mathematics.PurpleMath
import Strategery.History.HistoricalGame
import Strategery.Strategies.Strategy

class StrategyEvaluation(val strategy: Strategy) {

  // Hyperparameter controlling how many games worth of the optimistic target winrate to assume
  // Increase to revisit lost strategies sooner
  // Decrease to abandon them more aggressively
  val priorGames            : Double = 1.5
  val gamesAll              : Iterable[HistoricalGame]  = With.strategy.gamesVsOpponent
  val gamesAllWon           : Iterable[HistoricalGame]  = gamesAll.filter(_.won)
  val gamesAllLost          : Iterable[HistoricalGame]  = gamesAll.filterNot(_.won)
  val gamesUs               : Iterable[HistoricalGame]  = gamesAll.filter(_.weEmployed(strategy))
  val gamesUsWon            : Iterable[HistoricalGame]  = gamesUs.filter(_.won)
  val gamesUsLost           : Iterable[HistoricalGame]  = gamesUs.filterNot(_.won)
  val gamesUsWeightSum      : Double  = gamesUs     .map(_.weight).sum
  val gamesUsWonWeightSum   : Double  = gamesUsWon  .map(_.weight).sum
  val targetWins            : Double  = With.configuration.targetWinrate
  val winrateVsEnemy        : Double  = PurpleMath.nanToZero(gamesUsWonWeightSum / gamesUsWeightSum)

  // Probability we win given this strategy
  val probabilityWin: Double = (priorGames * targetWins + gamesUsWonWeightSum) / (priorGames + gamesUsWeightSum)
}

