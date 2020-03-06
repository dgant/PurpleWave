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
  val gamesUs               : Vector[HistoricalGame]  = With.strategy.gamesVsOpponent.filter(_.weEmployed(strategy)).toVector
  val gamesUsWon            : Vector[HistoricalGame]  = gamesUs.filter(_.won)
  val gamesUsLost           : Vector[HistoricalGame]  = gamesUs.filterNot(_.won)
  val gamesUsWeightSum      : Double  = gamesUs     .map(_.weight).sum
  val gamesUsWonWeightSum   : Double  = gamesUsWon  .map(_.weight).sum
  val targetWins            : Double  = With.configuration.targetWinrate
  val winrateVsEnemy        : Double  = PurpleMath.nanToZero(gamesUsWonWeightSum / gamesUsWeightSum)

  // Probability we win given this strategy
  val probabilityWin: Double = (priorGames * targetWins + gamesUsWonWeightSum) / (priorGames + gamesUsWeightSum)
}

