package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.Strategy

class StrategySelectionRecommended(fallback: StrategySelectionPolicy, recommendedBranch: Strategy*) extends StrategySelectionPolicy {

  var duration = 5

  override def toString: String = "StrategySelectionRecommended: (" + recommendedBranch.mkString(" + ") + ")"

  override def chooseBranch: Seq[Strategy] = {
    var legalMatchedBranches = With.strategy.strategyBranchesLegal.filter(branch => recommendedBranch.forall(branch.contains))

    if (legalMatchedBranches.isEmpty) {
      With.logger.warn(toString + " failed to find any matching branches filtered by legality")
      return fallback.chooseBranch
    }

    val gamesAgainst = With.history.gamesVsEnemies.size
    val gamesAgainstString = "game " + gamesAgainst + " of " + duration
    if (gamesAgainst < duration) {
      With.logger.debug(toString + " still in recommended strategy phase in " + gamesAgainstString)
      StrategySelectionGreedy(Some(legalMatchedBranches)).chooseBranch
    } else {
      With.logger.debug(toString + " has finished recommended strategy phase in " + gamesAgainstString + "; will fall back to " + fallback)
      fallback.chooseBranch
    }
  }
}
