package Strategery.Selection

import Strategery.Strategies.Strategy

case class StrategySelectionFixed(strategies: Strategy*) extends StrategySelectionRecommended(StrategySelectionGreedy(), 100000, 0.75, strategies: _*) {
  override def respectMap       : Boolean = false
  override def respectHistory   : Boolean = false

  override def toString: String = f"StrategySelectionFixed: $branchString"
}
