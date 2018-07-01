package Strategery.Selection

import Lifecycle.With
import Strategery.Playbook
import Strategery.Strategies.Strategy

class StrategySelectionRecommended(fallback: StrategySelectionPolicy, strategies: Strategy*) extends StrategySelectionFixed(strategies: _*) {

  override def chooseBest(topLevelStrategies: Iterable[Strategy]): Iterable[Strategy] = {
    val gamesAgainst = With.history.games.filter(_.enemyName == Playbook.enemyName)
    if (gamesAgainst.size < 5) {
      super.chooseBest(topLevelStrategies)
    }
    else {
      fallback.chooseBest(topLevelStrategies)
    }
  }
}
