package Strategery.Selection
import Lifecycle.With
import Strategery.Strategies.Strategy

case class StrategySelectionGreedy(requiredBranches: Option[Seq[Seq[Strategy]]] = None) extends StrategySelectionPolicy {

  lazy val branches: Seq[Seq[Strategy]] = requiredBranches.getOrElse(With.strategy.strategyBranchesLegal)

  def chooseBranch: Seq[Strategy] = {
    var eligibleBranches = branches.filter(_.forall(_.legality.isLegal))

    if (eligibleBranches.isEmpty && requiredBranches.nonEmpty) {
      With.logger.warn("Required branches which weren't legal; reverting to required branches without legality filtering.")
      eligibleBranches = requiredBranches.get
      if (eligibleBranches.isEmpty) {
        With.logger.warn("Required no branches! Reverting to default filtered branches")
        eligibleBranches = With.strategy.strategyBranchesLegal
      }
    }
    if (eligibleBranches.isEmpty) {
      With.logger.warn(toString + " has no legal branches! Reverting to unfiltered branches")
      eligibleBranches = With.strategy.strategyBranchesUnfiltered
    }

    pickFrom(eligibleBranches)
  }

  private def pickFrom(branches: Seq[Seq[Strategy]]): Seq[Strategy] = {
    val branchScores = branches.map(m => (m, With.strategy.winProbabilityByBranch.get(m)))
    branchScores.filter(_._2.isEmpty).foreach(missingScore => With.logger.warn("Missing win probability for: " + missingScore._1.mkString(" + ")))
    branchScores.maxBy(_._2.getOrElse(-1.0))._1
  }
}