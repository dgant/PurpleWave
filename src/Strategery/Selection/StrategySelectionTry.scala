package Strategery.Selection

import Strategery.Strategies.Strategy

class StrategySelectionTry(fallback: StrategySelectionPolicy, strategies: Strategy*) extends StrategySelectionRecommended(fallback, strategies: _*) {
  duration = 10000
}
