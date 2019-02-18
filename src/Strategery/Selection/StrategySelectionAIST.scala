package Strategery.Selection

import Lifecycle.With
import Strategery.Playbook
import Strategery.Strategies.Strategy

object StrategySelectionAIST extends StrategySelectionPolicy {
  
  def chooseBest(topLevelStrategies: Iterable[Strategy], expand: Boolean = true): Iterable[Strategy] = {
    val enemyName = Playbook.enemyName
    val opponent =
      OpponentsAIST.all.find(_.matches(enemyName)).orElse(
        OpponentsAIST.all.find(_.matchesLoosely(enemyName)).orElse(
          OpponentsAIST.all.find(_.matchesVeryLoosely(enemyName))))

    if (opponent.isEmpty) {
      With.logger.warn("Failed to find Opponent matching " + enemyName)
      return StrategySelectionGreedy.chooseBest(topLevelStrategies, expand)
    }

    opponent
      .map(_.policy.chooseBest(topLevelStrategies))
      .getOrElse(StrategySelectionGreedy.chooseBest(topLevelStrategies, expand))
  }
}
