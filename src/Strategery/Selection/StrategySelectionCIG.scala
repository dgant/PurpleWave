package Strategery.Selection

import Lifecycle.With
import Strategery.Plasma
import Strategery.Strategies.Strategy

object StrategySelectionCIG extends StrategySelectionPolicy {
  
  def chooseBest(topLevelStrategies: Iterable[Strategy]): Iterable[Strategy] = {
    if (Plasma.matches) {
      return StrategySelectionGreedy.chooseBest(topLevelStrategies)
    }
    
    val enemyName = With.enemy.name
    val opponent =
      Opponents.all.find(_.matches(enemyName)).orElse(
        Opponents.all.find(_.matchesLoosely(enemyName)).orElse(
          Opponents.all.find(_.matchesVeryLoosely(enemyName))))
    
    if (opponent.isEmpty) {
      With.logger.warn("Failed to find Opponent matching " + enemyName)
      return StrategySelectionGreedy.chooseBest(topLevelStrategies)
    }
    
    opponent
      .map(_.policy.chooseBest(topLevelStrategies))
      .getOrElse(StrategySelectionGreedy.chooseBest(topLevelStrategies))
  }
}
