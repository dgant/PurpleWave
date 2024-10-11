package Strategery.Selection

import Lifecycle.With
import Mathematics.Maff
import Strategery.Strategies.StrategyBranch

object StrategySelectionDynamic extends StrategySelectionPolicy {

  def chooseBranch: StrategyBranch = {
    sample(16)
  }

  def sample(probabilityExponent: Double): StrategyBranch = {
    val branches = Maff.orElse(With.strategy.strategyBranchesLegal, With.strategy.strategyBranchesAll).toSeq
    Maff.sampleWeighted(branches, (branch: StrategyBranch) => Math.pow(WinProbability(branch), probabilityExponent)).get
  }
}