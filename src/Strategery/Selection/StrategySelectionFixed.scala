package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.Strategy

class StrategySelectionFixed(fixedStrategies: Strategy*) extends StrategySelectionPolicy {

  var checkValidity: Boolean = true

  override def chooseBest(topLevelStrategies: Iterable[Strategy], expand: Boolean = true): Iterable[Strategy] = {
    if (checkValidity) {
      return fixedStrategies
    }
    val output = fixedStrategies.filter(With.strategy.isAppropriate)
    if (output.size < fixedStrategies.size) {
      StrategySelectionGreedy.chooseBest(topLevelStrategies, expand)
    }
    fixedStrategies
  }
}
