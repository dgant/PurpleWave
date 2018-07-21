package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.Strategy

object StrategySelectionSSCAIT extends StrategySelectionPolicy {
  
  def chooseBest(topLevelStrategies: Iterable[Strategy]): Iterable[Strategy] = {
    val history = With.history.gamesVsEnemies

    val recentHistory = history.toVector.sortBy(-_.timestamp).take(5)
    if (recentHistory.forall(_.won)) {
      StrategySelectionGreedy.chooseBest(topLevelStrategies)
    }
    else {
      StrategySelectionDynamic.chooseBest(topLevelStrategies)
    }
  }
}
