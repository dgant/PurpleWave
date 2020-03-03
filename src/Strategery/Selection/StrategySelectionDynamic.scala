package Strategery.Selection

import Lifecycle.With
import Mathematics.PurpleMath
import Strategery.Strategies.Strategy

object StrategySelectionDynamic extends StrategySelectionPolicy {

  def chooseBranch: Seq[Strategy] = {
    val weights = With.strategy.strategyBranchesLegal
      .map(branch =>
        (
          branch,
          Math.exp(With.configuration.dynamicStickiness * With.strategy.winProbabilityByBranch(branch)
        )))
      .toMap
    PurpleMath.sampleWeighted(weights.keys.toSeq, w => weights(w)).get
  }
}