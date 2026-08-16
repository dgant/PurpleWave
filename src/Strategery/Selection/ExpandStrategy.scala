package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.{Strategy, StrategyBranch}
import bwapi.Race

object ExpandStrategy {
  def apply(root: Strategy): Seq[StrategyBranch] = apply(root, With.enemy.raceCurrent)

  private def apply(root: Strategy, enemyRace: Race): Seq[StrategyBranch] = {
    if (root == null) {
      throw new NullPointerException()
    }
    if (root.choices == null) {
      throw new NullPointerException()
    }
    if (root.choices.contains(null)) {
      throw new NullPointerException()
    }
    val choices = root.choices
      .filter(choice => choice.strategies.nonEmpty)
      .filter(choice => enemyRace == Race.Unknown || choice.enemyRace.isEmpty || choice.enemyRace.contains(enemyRace))

    if (choices.isEmpty) {
      return Seq(StrategyBranch(Seq(root)))
    }

    val choicesChain = choices.map(_.strategies.flatMap(apply(_, enemyRace)))

    var output = Seq(StrategyBranch(Seq(root)))
    choicesChain.foreach(nextChoiceChain =>
      output = output.flatMap(outputChain =>
        nextChoiceChain.map(nextChoice =>
          StrategyBranch(outputChain.strategies ++ nextChoice.strategies)))
    )

    output.distinct
  }
}
