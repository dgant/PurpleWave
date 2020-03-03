package Strategery.Selection

import Strategery.Strategies.Strategy

trait StrategySelectionPolicy {
  def chooseBranch: Seq[Strategy]
}
