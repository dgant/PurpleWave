package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.Protoss._
import Strategery.Strategies.Strategy

object StrategySelectionSSCAIT extends StrategySelectionPolicy {
  
  def chooseBest(topLevelStrategies: Iterable[Strategy], expand: Boolean = true): Iterable[Strategy] = {
    val history = With.history.gamesVsEnemies

    if (With.enemy.isTerran) {
      if (history.size % 2 == 0) {
        return StrategySelectionGreedy.chooseBest(Vector(PvT23Nexus, PvT28Nexus, PvT2GateRangeExpand), expand = false) ++ Vector(PvT2BaseCarrier)
      } else {
        return (
          if (With.strategy.allowedGivenOpponentHistory(PvTDTExpand))
            Vector(PvTDTExpand)
          else
            StrategySelectionGreedy.chooseBest(Vector(PvT23Nexus, PvT28Nexus, PvT2GateRangeExpand), expand = false)
          ) ++ Vector(PvT2BaseArbiter)
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
