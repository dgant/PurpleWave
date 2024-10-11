package Strategery.Selection
import Lifecycle.With
import Strategery.Strategies.{Strategy, StrategyBranch}

case class StrategySelectionGreedy(requiredBranches: Option[Seq[Seq[Strategy]]] = None) extends StrategySelectionPolicy {

  lazy val branches: Seq[StrategyBranch] = requiredBranches
    .map(rbs => rbs.flatMap(With.strategy.matchBranches))
    .getOrElse(With.strategy.strategyBranchesLegal)

  def chooseBranch: StrategyBranch = {
    var eligibleBranches = branches.filter(_.legal)

    if (eligibleBranches.isEmpty && requiredBranches.nonEmpty) {
      With.logger.warn("Required branches which weren't legal; reverting to required branches without legality filtering.")
      eligibleBranches = branches
      if (eligibleBranches.isEmpty) {
        With.logger.warn("Required no branches! Reverting to default filtered branches")
        eligibleBranches = With.strategy.strategyBranchesLegal
      }
    }
    if (eligibleBranches.isEmpty) {
      With.logger.warn(toString + " has no legal branches! Reverting to unfiltered branches")
      eligibleBranches = With.strategy.strategyBranchesAll
    }

    eligibleBranches.maxBy(_.winProbability)
  }
}