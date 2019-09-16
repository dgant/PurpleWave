package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.Strategy

class StrategySelectionRecommended(fallback: StrategySelectionPolicy, strategies: Strategy*) extends StrategySelectionFixed(strategies: _*) {

  var duration = 5

  override def chooseBest(topLevelStrategies: Iterable[Strategy], expand: Boolean = true): Iterable[Strategy] = {
    val gamesAgainst = With.history.gamesVsEnemies
    if (gamesAgainst.size < duration && strategies.forall(With.strategy.isAppropriate)) {
      super.chooseBest(topLevelStrategies, expand)
    }
    else {
      fallback.chooseBest(topLevelStrategies, expand)
    }
  }
}
