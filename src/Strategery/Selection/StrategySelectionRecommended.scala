package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.{Strategy, StrategyBranch}

class StrategySelectionRecommended(fallback: StrategySelectionPolicy, recommendedBranch: Strategy*) extends StrategySelectionPolicy {

  var duration = 5

  override def respectMap     : Boolean = true
  override def respectHistory : Boolean = false

  def this(fallback: StrategySelectionPolicy, duration: Int, recommendedBranch: Strategy*) {
    this(fallback, recommendedBranch: _*)
    this.duration = duration
  }

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
