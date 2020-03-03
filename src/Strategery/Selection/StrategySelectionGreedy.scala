package Strategery.Selection
import Lifecycle.With
import Strategery.Strategies.Strategy

case class StrategySelectionGreedy(requiredBranches: Option[Seq[Seq[Strategy]]] = None) extends StrategySelectionPolicy {
  override def toString: String = "StrategySelectionRecommended: (" + branches.map(_.mkString(" + ")).mkString(", ") + ")"

  lazy val branches: Seq[Seq[Strategy]] = requiredBranches.getOrElse(With.strategy.strategyBranchesUnfiltered)

  def chooseBranch: Seq[Strategy] = {
    var legalBranches = branches.filter(_.forall(_.legality.isLegal))

    if (legalBranches.isEmpty) {
      With.logger.warn(toString + " has no legal branches! ")
      legalBranches = With.strategy.strategyBranchesLegal
    }
    if (legalBranches.isEmpty) {
      With.logger.warn(toString + " still has no legal branches! ")
      legalBranches = With.strategy.strategyBranchesUnfiltered
    }

    val branchScores = legalBranches.map(m => (m, With.strategy.winProbabilityByBranch.get(m)))
    branchScores.filter(_._2.isEmpty).foreach(missingScore => With.logger.warn("Missing win probability for: " + missingScore._1.mkString(" + ")))
    branchScores.maxBy(_._2.getOrElse(-1.0))._1
  }
}