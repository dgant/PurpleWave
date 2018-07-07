package Strategery.Selection

import Lifecycle.With
import Strategery.Playbook
import Strategery.Strategies.Strategy

class StrategySelectionRecommended(fallback: StrategySelectionPolicy, strategies: Strategy*) extends StrategySelectionFixed(strategies: _*) {

  override def chooseBest(topLevelStrategies: Iterable[Strategy]): Iterable[Strategy] = {
    val gamesAgainst = With.history.games.filter(game => With.strategy.nameMatches(game.enemyName, Playbook.enemyName))
    if (gamesAgainst.size < 5 && strategies.forall(With.strategy.isAppropriate)) {
      super.chooseBest(topLevelStrategies)
    }
    else {
      fallback.chooseBest(topLevelStrategies)
    }
  }
}
