package Strategery.Selection

import Strategery.Strategies.Strategy

class StrategySelectionFixedUnfiltered(fixedStrategies: Strategy*) extends StrategySelectionPolicy {

  override def chooseBest(topLevelStrategies: Iterable[Strategy], expand: Boolean = true): Iterable[Strategy] = {
    fixedStrategies
  }
}
