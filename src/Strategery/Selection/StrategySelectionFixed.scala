package Strategery.Selection

import Strategery.Strategies.Strategy

case class StrategySelectionFixed(fixedStrategies: Strategy*) extends StrategySelectionPolicy {
  
  override def chooseBest(topLevelStrategies: Iterable[Strategy]): Iterable[Strategy] = fixedStrategies
}
