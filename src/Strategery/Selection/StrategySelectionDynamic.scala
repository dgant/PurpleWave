package Strategery.Selection

import Lifecycle.With
import Mathematics.Maff
import Strategery.Strategies.Strategy

object StrategySelectionDynamic extends StrategySelectionPolicy {

  def chooseBranch: Seq[Strategy] = {
    sample(16)
  }

  def sample(probabilityExponent: Double): Seq[Strategy] = {
    val branches = With.strategy.strategyBranchesLegal
    Maff
      .sampleWeighted(
        branches,
        (branch: Seq[Strategy]) =>
          Math.pow(WinProbability(branch), probabilityExponent)
          / Maff.geometricMean(branch.map(strategy => branches.count(_.contains(strategy)).toDouble)))
      .getOrElse(Seq.empty)
  }
}