package Strategery.Selection

import Strategery.Strategies.Strategy

object ExpandStrategy {
  def apply(root: Strategy): Seq[Strategy] = {
    root.choices.flatMap(_.flatMap(apply)).toSeq.distinct
  }
}
