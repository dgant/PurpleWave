package Strategery.Selection

import Strategery.History.HistoricalGame
import Strategery.Strategies.StrategyBranch

class WeightedGame(val branch: StrategyBranch, val game: HistoricalGame) {
  val similarity  : Double = branch.strategies.count(game.weEmployed).toDouble / branch.strategies.length
  val finalWeight : Double = similarity * game.weight
}
