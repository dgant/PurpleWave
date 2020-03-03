package Strategery.Selection

import Lifecycle.With
import Mathematics.PurpleMath
import Strategery.Strategies.Strategy

object StrategySelectionRandom extends StrategySelectionPolicy {

  def chooseBranch: Seq[Strategy] = {
    PurpleMath.sample(With.strategy.strategyBranchesLegal)
  }
}