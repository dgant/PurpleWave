package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.Strategy

class StrategySelectionFixed(fixedStrategies: Strategy*) extends StrategySelectionPolicy {
  
  override def chooseBest(topLevelStrategies: Iterable[Strategy], expand: Boolean = true): Iterable[Strategy] = {
    val output = topLevelStrategies.filter(With.strategy.isAppropriate)
    if (output.isEmpty) {
      StrategySelectionGreedy.chooseBest(topLevelStrategies, expand)
    }
    fixedStrategies
  }
}
