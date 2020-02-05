package Strategery.Selection

import Lifecycle.With
import Strategery.{Plasma, FinalPlaybook}
import Strategery.Strategies.Strategy

object StrategySelectionTournament extends StrategySelectionPolicy {
  
  def chooseBest(topLevelStrategies: Iterable[Strategy], expand: Boolean = true): Iterable[Strategy] = {
    if (Plasma.matches) {
      return StrategySelectionGreedy.chooseBest(topLevelStrategies, expand)
    }
    
    val enemyName = FinalPlaybook.enemyName
    val opponent =
      Opponents.all.find(_.matches(enemyName)).orElse(
        Opponents.all.find(_.matchesLoosely(enemyName)).orElse(
          Opponents.all.find(_.matchesVeryLoosely(enemyName))))
    
    if (opponent.isEmpty) {
      With.logger.warn("Failed to find Opponent matching " + enemyName)
      return StrategySelectionGreedy.chooseBest(topLevelStrategies, expand)
    }
    
    opponent
      .map(_.policy.chooseBest(topLevelStrategies))
      .getOrElse(StrategySelectionGreedy.chooseBest(topLevelStrategies, expand))
  }
}
