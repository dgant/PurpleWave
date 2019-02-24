package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.Strategy
import Utilities.ByOption

case class StrategySelectionSequence(strategySequences: IndexedSeq[Seq[Strategy]], loop: Boolean = false) extends StrategySelectionPolicy {

  override def chooseBest(topLevelStrategies: Iterable[Strategy], expand: Boolean): Iterable[Strategy] = {
    val gamesAgainst = With.history.gamesVsEnemies

    val appropriate = strategySequences.filter(_.forall(With.strategy.isAppropriate))

    if ( ! loop && appropriate.forall(_.forall(strategy => gamesAgainst.exists(_.weEmployed(strategy))))) {
      return StrategySelectionGreedy.chooseBest(
        topLevelStrategies.filter(strategy => appropriate.exists(_.exists(_ == strategy))),
        expand)
    }

    val leastUsed = ByOption.minBy(appropriate)(strategies =>
      strategies
        .map(strategy => gamesAgainst.count(_.weEmployed(strategy)))
        .min)

    if (leastUsed.nonEmpty) {
      val fixed = new StrategySelectionFixed(leastUsed.get: _*)
      return fixed.chooseBest(topLevelStrategies, expand)
    }

    // Just in case
    StrategySelectionGreedy.chooseBest(topLevelStrategies, expand)
  }
}