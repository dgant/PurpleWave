package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.Strategy

class StrategySelectionFixed(fixedStrategies: Strategy*) extends StrategySelectionPolicy {
  
  override def chooseBest(topLevelStrategies: Iterable[Strategy]): Iterable[Strategy] = {
    fixedStrategies
  }
}
