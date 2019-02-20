package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.Strategy

case class StrategySelectionSequence(strategySequences: IndexedSeq[Seq[Strategy]], loop: Boolean = false) extends StrategySelectionPolicy {

  override def chooseBest(topLevelStrategies: Iterable[Strategy], expand: Boolean): Iterable[Strategy] = {
    val gamesAgainst = With.history.gamesVsEnemies

    val appropriate = strategySequences.filter(_.forall(With.strategy.isAppropriate))

    if ( ! loop && appropriate.headOption.forall(_.forall(strategy => gamesAgainst.exists(_.weEmployed(strategy))))) {
      return StrategySelectionGreedy.chooseBest(
        topLevelStrategies.filter(strategy =>
          appropriate.exists(_.exists(_ == strategy))),
        expand)
    }

    val appropriateSorted = appropriate.sortBy(strategies =>
      strategies
        .map(strategy => gamesAgainst.count(_.weEmployed(strategy)))
        .min)

    val fixed = new StrategySelectionFixed(appropriateSorted.head: _*)

    fixed.chooseBest(topLevelStrategies, expand)
  }
}