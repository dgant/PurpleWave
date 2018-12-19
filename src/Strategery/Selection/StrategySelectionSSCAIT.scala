package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.Protoss._
import Strategery.Strategies.Strategy

object StrategySelectionSSCAIT extends StrategySelectionPolicy {
  
  def chooseBest(topLevelStrategies: Iterable[Strategy]): Iterable[Strategy] = {
    val history = With.history.gamesVsEnemies

    // TODO: Enable ReaverCarrierCheese, etc
    if (With.enemy.isTerran) {
      if (history.size % 2 == 0) {
        return Vector(
          if (With.strategy.allowedGivenOpponentHistory(PvT13Nexus)) PvT13Nexus else PvT21Nexus,
          PvT2BaseCarrier)
      } else {
        return Vector(
          if (With.strategy.allowedGivenOpponentHistory(PvTDTExpand)) PvTDTExpand else PvT21Nexus,
          PvT2BaseArbiter)
      }
    }

    // Explore during the round robin
    if (history.size < 3) {
      return StrategySelectionRandom.chooseBest(topLevelStrategies)
    }

    val recentHistory = history.toVector.sortBy(-_.timestamp).take(2)
    if (recentHistory.forall(_.won)) {
      StrategySelectionGreedy.chooseBest(topLevelStrategies)
    }
    else {
      StrategySelectionDynamic.chooseBest(topLevelStrategies)
    }
  }
}
