package Strategery.Selection

import Strategery.Strategies.Strategy

object StrategySelectionDynamic extends StrategySelectionPolicy {
  def chooseBest(topLevelStrategies: Iterable[Strategy]): Iterable[Strategy] = {
    StrategySelectionGreedy.chooseBest(topLevelStrategies)
  }
}
