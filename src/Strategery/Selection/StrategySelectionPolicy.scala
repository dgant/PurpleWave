package Strategery.Selection

import Strategery.Strategies.Strategy

trait StrategySelectionPolicy {
  def chooseBestUnfiltered(topLevelStrategies: Iterable[Strategy], expand: Boolean = true): Option[Iterable[Strategy]] = None
  def chooseBest(topLevelStrategies: Iterable[Strategy], expand: Boolean = true): Iterable[Strategy]
}
