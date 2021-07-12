package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.Strategy
import bwapi.Race

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

    var default: StrategySelectionPolicy = StrategySelectionGreedy()
    if (With.self.raceCurrent == Race.Protoss) {
      if (With.enemy.raceInitial == Race.Terran) default= Opponents.defaultPvT
      else if (With.enemy.raceInitial == Race.Protoss) default = Opponents.defaultPvP
      else if (With.enemy.raceInitial == Race.Zerg) default = Opponents.defaultPvZ
    }
    
    opponent.map(_.policy).getOrElse(default).chooseBranch
  }
}
