package Strategery.Selection

import Strategery.Strategies.{Strategy, StrategyBranch}

object ExpandStrategy {
  def apply(root: Strategy): Seq[StrategyBranch] = {
    if (root == null) {
      throw new NullPointerException()
    }
    if (root.choices == null) {
      throw new NullPointerException()
    }
    if (root.choices.contains(null)) {
      throw new NullPointerException()
    }
    val choices = root.choices.filter(_.nonEmpty)

    if (choices.isEmpty) {
      return Seq(StrategyBranch(Seq(root)))
    }

    val choicesChain = choices.map(_.flatMap(apply))

    var output = Seq(StrategyBranch(Seq(root)))
    choicesChain.foreach(nextChoiceChain =>
      output = output.flatMap(outputChain =>
        nextChoiceChain.map(nextChoice =>
          StrategyBranch(outputChain.strategies ++ nextChoice.strategies)))
    )

    output.distinct
  }
}
