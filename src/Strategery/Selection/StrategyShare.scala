package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.{Strategy, StrategyBranch}

object StrategyShare {

  def byStrategy: Map[Strategy, Double] = {
    val branches = With.strategy.strategyBranchesLegal
    branches
      .flatMap(_.strategies)
      .distinct
      .map(strategy => (
        strategy,
        branches.map(branch => if (branch.strategies.contains(strategy)) 1.0 / branch.strategies.length else 0.0).sum
      ))
      .toMap
  }

  def byBranch: Map[StrategyBranch, Double] = {
    val strategies = byStrategy
    With.strategy.strategyBranchesLegal
      .map(branch => (
        branch,
        branch.strategies.map(strategies).sum
      ))
      .toMap
  }
}
