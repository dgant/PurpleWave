package Strategery.Selection

import Strategery.Strategies.Strategy

case class StrategySelectionFixed(strategies: Strategy*) extends StrategySelectionRecommended(StrategySelectionGreedy(), strategies: _*) {
  duration = 100000
  override def respectMap       : Boolean = false
  override def respectHistory   : Boolean = false
}
