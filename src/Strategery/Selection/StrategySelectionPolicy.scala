package Strategery.Selection

import Debugging.SimpleString
import Strategery.Strategies.Strategy

trait StrategySelectionPolicy extends SimpleString {
  def chooseBranch: Seq[Strategy]
}
