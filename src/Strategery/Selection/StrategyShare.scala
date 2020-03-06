package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.Strategy

object StrategyShare {

  def byStrategy: Map[Strategy, Double] = {
    val branches = With.strategy.strategyBranchesLegal
    branches
      .flatten
      .distinct
      .map(strategy => (
        strategy,
        branches.map(branch => if (branch.contains(strategy)) 1.0 / branch.length else 0.0).sum
      ))
      .toMap
  }

  def byBranch: Map[Seq[Strategy], Double] = {
    val strategies = byStrategy
    With.strategy.strategyBranchesLegal
      .map(branch => (
        branch,
        branch.map(strategies).sum
      ))
      .toMap
  }
}
