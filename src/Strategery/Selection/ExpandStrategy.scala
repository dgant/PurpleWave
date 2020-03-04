package Strategery.Selection

import Strategery.Strategies.Strategy

object ExpandStrategy {
  def apply(root: Strategy): Seq[Seq[Strategy]] = {
    val choices = root.choices.filter(_.nonEmpty)

    if (choices.isEmpty) {
      return Seq(Seq(root))
    }

    val choicesChain = choices.map(_.flatMap(apply))

    var output = Seq(Seq(root))
    choicesChain.foreach(nextChoiceChain =>
      output = output.flatMap(outputChain =>
        nextChoiceChain.map(nextChoice =>
          outputChain ++ nextChoice))
    )

    output.distinct
  }
}
