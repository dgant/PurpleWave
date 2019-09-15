package Strategery.Selection

import Strategery.Strategies.Strategy

class StrategySelectionFixedUnfiltered(fixedStrategies: Strategy*) extends StrategySelectionPolicy {

  override def chooseBestUnfiltered(topLevelStrategies: Iterable[Strategy], expand: Boolean = true): Option[Iterable[Strategy]] = {
    Some(fixedStrategies)
  }

  override def chooseBest(topLevelStrategies: Iterable[Strategy], expand: Boolean): Iterable[Strategy] = fixedStrategies
}
