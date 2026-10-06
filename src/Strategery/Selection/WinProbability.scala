package Strategery.Selection

import Lifecycle.With
import Strategery.Strategies.StrategyBranch
import bwapi.Race

import scala.util.Random

object WinProbability {

  private var cachedKey: Option[(Long, Double, Double, Int)] = None
  private var cachedModel: StrategyScore.Model = _

  private def model: StrategyScore.Model = {
    val config = With.configuration
    val key = (With.id, config.targetWinrate, config.historyHalfLife, config.recentFingerprints)
    if (!cachedKey.contains(key)) {
      val names = With.strategy.strategiesAll.map(_.toString).toSet
      val games = With.strategy.gamesVsOpponent.map(game =>
        StrategyScore.Game(game.won, game.tags.filter(names.contains)))
      cachedModel = StrategyScore.fit(games, config.targetWinrate, config.historyHalfLife, config.recentFingerprints)
      cachedKey = Some(key)
    }
    cachedModel
  }

  def apply(strategyBranch: StrategyBranch): Double = {
    val fitted = model
    // Random-opponent branches contain mutually exclusive continuations for all three races.
    // Average their predictions instead of adding effects which cannot occur together.
    val races = if (With.enemy.raceCurrent == Race.Unknown)
      Seq(Race.Protoss, Race.Terran, Race.Zerg) else Seq(With.enemy.raceCurrent)
    val scores = races.map(race => fitted(strategyBranch.strategies
      .filter(strategy => strategy.enemyRaces.contains(race) || strategy.enemyRaces.contains(Race.Unknown)).map(_.toString)))
    noise(scores.sum / scores.size)
  }

  private def noise(input: Double): Double = {
    val noiseWeight = 1e-6
    Random.nextDouble() * noiseWeight + input * (1.0 - noiseWeight)
  }
}
