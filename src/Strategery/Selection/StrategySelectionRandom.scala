package Strategery.Selection

import Lifecycle.With
import Mathematics.Maff
import Strategery.Strategies.StrategyBranch

object StrategySelectionRandom extends StrategySelectionPolicy {

  def chooseBranch: StrategyBranch = {
    Maff.sampleWeighted(With.strategy.strategyBranchesLegal, (b: StrategyBranch) => b.explorationWeight).get
  }

  override def toString = "StrategySelectionRandom"
}