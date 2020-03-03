package Strategery.Selection

import Strategery.Strategies.Strategy

case class StrategySelectionFixed(strategies: Strategy*) extends StrategySelectionRecommended(StrategySelectionGreedy(), strategies: _*) {
  duration = 100000
}
