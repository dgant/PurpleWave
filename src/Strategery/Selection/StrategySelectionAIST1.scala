package Strategery.Selection
import Lifecycle.With
import Strategery.Strategies.Strategy

object StrategySelectionAIST1 extends StrategySelectionPolicy {
  
  override def chooseBest(topLevelStrategies: Iterable[Strategy]): Iterable[Strategy] = {
    val allowed = With.strategy.filterStrategies(topLevelStrategies)
    val output = allowed.headOption
    allowed
  }
  
}
