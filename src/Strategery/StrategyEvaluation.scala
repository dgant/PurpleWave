package Strategery

import Lifecycle.With
import Mathematics.Maff
import Strategery.History.HistoricalGame
import Strategery.Strategies.Strategy

class StrategyEvaluation(val strategy: Strategy) {
  val games             : Vector[HistoricalGame]  = With.strategy.gamesVsOpponent.filter(_.weEmployed(strategy)).toVector
  val gamesWon          : Vector[HistoricalGame]  = games.filter(_.won)
  val gamesWeighted     : Double                  = games     .map(_.weight).sum
  val gamesWeightedWon  : Double                  = gamesWon  .map(_.weight).sum
  val winrate           : Double                  = Maff.nanToN(gamesWon.length.toDouble  / games.length,   With.configuration.targetWinrate)
  val winrateWeighted   : Double                  = Maff.nanToN(gamesWeightedWon          / gamesWeighted,  With.configuration.targetWinrate)
}

