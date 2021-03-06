package Strategery.Selection

import Lifecycle.With
import Mathematics.PurpleMath
import Strategery.Strategies.Strategy

object StrategySelectionDynamic extends StrategySelectionPolicy {

  def chooseBranch: Seq[Strategy] = {
    val weights = StrategyShare.byBranch
      .map(branch => (
        branch._1,
        Math.exp(With.configuration.dynamicStickiness * With.strategy.winProbabilityByBranch(branch._1)) / branch._2
      ))
    PurpleMath.sampleWeighted(weights.keys.toSeq, w => weights(w)).get
  }
}