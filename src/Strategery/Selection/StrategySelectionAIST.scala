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
      With.logger.warn("Failed to find opponent matching " + enemyName)
      return (
        if (With.enemy.isTerran) {
          OpponentsAIST.defaultPvT
        } else if (With.enemy.isProtoss) {
          OpponentsAIST.defaultPvP
        } else if (With.enemy.isZerg) {
          OpponentsAIST.defaultPvZ
        } else StrategySelectionGreedy)
        .chooseBest(topLevelStrategies, expand)
    }

    opponent.get.policy.chooseBest(topLevelStrategies)
  }
}
