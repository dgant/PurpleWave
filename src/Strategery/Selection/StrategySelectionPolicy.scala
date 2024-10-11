package Strategery.Selection

import Debugging.SimpleString
import Strategery.Strategies.StrategyBranch

trait StrategySelectionPolicy extends SimpleString {
  def chooseBranch: StrategyBranch
  def respectMap      : Boolean = true
  def respectHistory  : Boolean = true
}
