package Strategery

import Lifecycle.With
import Mathematics.Maff
import Strategery.History.HistoricalGame
import Strategery.Selection.ExpandStrategy
import Strategery.Strategies.{AllChoices, Strategy, StrategyBranch}
import bwapi.Race

trait StrategyDatabase {
  lazy val gameWeights: Map[HistoricalGame, Double] = With.history.games
    .filter(_.enemyMatches)
    .zipWithIndex
    .map { case (game, age) =>
      val relevanceTimeDecay  : Double = Math.pow(0.98, age)
      val relevanceRecentLoss : Double = Maff.fromBoolean(age < With.configuration.recentFingerprints && !game.won)
      val relevanceAge        : Double = 0.25 + 0.5 * relevanceTimeDecay + 0.25 * relevanceRecentLoss
      (game, relevanceAge)
    }
    .toMap

  lazy val strategiesTopLevel     : Seq[Strategy]       = if (With.enemy.raceCurrent == Race.Unknown) AllChoices.treeVsRandom else AllChoices.treeVsKnownRace
  lazy val strategiesAll          : Seq[Strategy]       = strategyBranchesAll.flatMap(_.strategies).distinct
  lazy val strategyBranchesAll    : Seq[StrategyBranch] = strategiesTopLevel.flatMap(ExpandStrategy.apply).distinct
  lazy val strategyBranchesLegal  : Seq[StrategyBranch] = strategyBranchesAll.filter(_.strategies.forall(_.legal))
  lazy val gamesVsOpponent        : Seq[HistoricalGame] = With.history.games.filter(With.enemies.size == 1 && _.enemyName == With.configuration.playbook.enemyName)

  def matchNames(names: Seq[String], branches: Seq[StrategyBranch]): Seq[StrategyBranch] = {
    strategyBranchesAll.filter(branch => names.forall(name => branch.strategies.exists(_.toString.toLowerCase == name.toLowerCase)))
  }

  def matchBranches(strategies: Seq[Strategy]): Seq[StrategyBranch] = {
    strategyBranchesAll.filter(branch => strategies.forall(branch.strategies.contains))
  }
}
