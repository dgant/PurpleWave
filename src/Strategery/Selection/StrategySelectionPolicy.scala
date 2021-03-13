package Strategery.Selection

import Debugging.SimpleString
import Strategery.Strategies.Strategy

trait StrategySelectionPolicy extends SimpleString {
  def chooseBranch: Seq[Strategy]
  def respectMap: Boolean = true
  def respectHistory: Boolean = true
}
