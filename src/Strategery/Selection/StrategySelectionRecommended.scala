package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.{Strategy, StrategyBranch}

class StrategySelectionRecommended(fallback: StrategySelectionPolicy, val duration: Int, override val targetWinrate: Double, recommendedBranch: Strategy*) extends StrategySelectionPolicy {

  override def respectMap     : Boolean = true
  override def respectHistory : Boolean = false

  override def chooseBranch: StrategyBranch = {
    val legalMatchedBranches = With.strategy.strategyBranchesLegal
      .filter(branch => recommendedBranch.forall(branch.strategies.contains))
      .sortBy(_.strategies.length)

    if (legalMatchedBranches.isEmpty) {
      With.logger.warn(f"$this failed to find any branches, filtered by legality, matching ${recommendedBranch.mkString(" + ")}")
      return fallback.chooseBranch
    }

    val gamesAgainst = With.history.gamesVsEnemies.size
    val gamesAgainstString = "game " + gamesAgainst + " of " + duration
    if (gamesAgainst < duration) {
      With.logger.debug(toString + " still in recommended strategy phase in " + gamesAgainstString)
      StrategySelectionGreedy(Some(legalMatchedBranches.map(_.strategies))).chooseBranch
    } else {
      With.logger.debug(toString + " has finished recommended strategy phase in " + gamesAgainstString + "; will fall back to " + fallback)
      fallback.chooseBranch
    }
  }

  override def toString = f"StrategySelectionRecommended: $branchString, falling back to $fallback"
  protected def branchString: String = recommendedBranch.map(_.toString).mkString(" + ")
}
