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
        return Vector(PvT13Nexus, PvT2BaseCarrier)
      } else {
        return Vector(PvTDTExpand, PvT2BaseArbiter)
      }
    }

    val recentHistory = history.toVector.sortBy(-_.timestamp).take(5)
    if (recentHistory.forall(_.won)) {
      StrategySelectionGreedy.chooseBest(topLevelStrategies)
    }
    else {
      StrategySelectionDynamic.chooseBest(topLevelStrategies)
    }
  }
}
