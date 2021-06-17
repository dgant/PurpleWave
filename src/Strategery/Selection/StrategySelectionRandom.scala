package Strategery.Selection

import Mathematics.Maff
import Strategery.Strategies.Strategy

object StrategySelectionRandom extends StrategySelectionPolicy {

  def chooseBranch: Seq[Strategy] = {
    // Select randomly, weighed by frequency of components
    val weights = StrategyShare.byBranch
      .map(branch => (
        branch._1,
        1.0 / branch._2
      ))
    Maff.sampleWeighted(weights.keys.toSeq, w => weights(w)).get
  }

  override def toString = "StrategySelectionRandom"
}