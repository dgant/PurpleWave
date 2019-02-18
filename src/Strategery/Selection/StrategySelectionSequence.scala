package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.Strategy

case class StrategySelectionSequence(strategySequences: IndexedSeq[Seq[Strategy]], loop: Boolean = false) extends StrategySelectionPolicy {

  override def chooseBest(topLevelStrategies: Iterable[Strategy], expand: Boolean): Iterable[Strategy] = {
    val gamesAgainst = With.history.gamesVsEnemies.size

    val strategySequencesAllowed = strategySequences.filter(_.forall(With.strategy.isAppropriate))

    if (gamesAgainst >= strategySequencesAllowed.size && ! loop) {
      return StrategySelectionGreedy.chooseBest(
        topLevelStrategies.filter(strategy =>
          strategySequencesAllowed.exists(_.exists(_ == strategy))),
        expand)
    }

    val strategies = strategySequencesAllowed(gamesAgainst % strategySequencesAllowed.size)
    val fixed = new StrategySelectionFixed(strategies: _*)

    fixed.chooseBest(topLevelStrategies, expand)
  }
}