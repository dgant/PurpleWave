package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.Strategy

object StrategySelectionTournament extends StrategySelectionPolicy {
  
  def chooseBranch: Seq[Strategy] = {
    
    val enemyName = With.configuration.playbook.enemyName
    val opponent =
      Opponents.all.find(_.matches(enemyName)).orElse(
        Opponents.all.find(_.matchesLoosely(enemyName)).orElse(
          Opponents.all.find(_.matchesVeryLoosely(enemyName))))
    
    if (opponent.isEmpty) {
      With.logger.warn("Didn't find opponent plan for " + enemyName)
    }
    
    opponent.map(_.policy.chooseBranch).getOrElse(StrategySelectionGreedy().chooseBranch)
  }
}
