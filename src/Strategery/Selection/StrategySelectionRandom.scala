package Strategery.Selection

import Mathematics.Maff
import Strategery.Strategies.{Strategy, StrategyBranch}

object StrategySelectionRandom extends StrategySelectionPolicy {

  def chooseBranch: StrategyBranch = {
    // Select randomly, weighed by frequency of components
    val weights = StrategyShare.byBranch
      .map(branch => (
        branch._1,
        1.0 / branch._2
      ))
    Maff.sampleWeighted(weights.keys.toSeq, weights).get
  }

  override def toString = "StrategySelectionRandom"
}