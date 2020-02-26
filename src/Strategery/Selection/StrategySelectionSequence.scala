package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.Strategy
import Utilities.ByOption

case class StrategySelectionSequence(strategySequences: Seq[Seq[Strategy]], loop: Boolean = false) extends StrategySelectionPolicy {

  override def chooseBest(topLevelStrategies: Iterable[Strategy], expand: Boolean = true): Iterable[Strategy] = {
    val gamesAgainst = With.history.gamesVsEnemies

    val appropriateness = strategySequences.map(seq => seq.map(strategy => (strategy, With.strategy.isAppropriate(strategy))))
    val appropriate = strategySequences.filter(_.forall(With.strategy.isAppropriate))
    With.logger.debug("StrategySelectionSequence appropriateness:")
    With.logger.debug(appropriateness.toString)

    if (appropriate.isEmpty) {
      With.logger.warn("No complete strategies in StrategySelectionSequence were appropriate.")
    }

    if ( ! loop && appropriate.forall(_.forall(strategy => gamesAgainst.exists(_.weEmployed(strategy))))) {
      With.logger.debug("StrategySelectionSequence has tried everything once and isn't looping. We will choose from among them greedily.")
      return StrategySelectionGreedy.chooseBest(appropriate)
    }

    val leastUsed = ByOption.minBy(appropriate)(strategies =>
      strategies
        .map(strategy => gamesAgainst.count(_.weEmployed(strategy)))
        .min)

    if (leastUsed.nonEmpty) {
      With.logger.debug("StrategySelectionSequence will select the least-used strategy from the sequence.")
      val fixed = StrategySelectionFixed(leastUsed.get: _*)
      return fixed.chooseBest(topLevelStrategies, expand)
    }

    // Just in case
    With.logger.warn("Couldn't follow any strategies in StrategySelectionSequence. We will instead choose greedily from all possible builds.")
    StrategySelectionGreedy.chooseBest(topLevelStrategies, expand)
  }
}