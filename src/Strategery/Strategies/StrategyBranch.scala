package Strategery.Strategies

import Strategery.Selection.WinProbability

case class StrategyBranch(strategies: Seq[Strategy]) {

  override def toString: String = strategies.map(_.toString).mkString(" + ")

  lazy val winProbability: Double = WinProbability(this)

  def legal: Boolean = strategies.forall(_.legal)
}
