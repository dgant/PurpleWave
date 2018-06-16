package Strategery.Selection

import Lifecycle.With
import Strategery.Sparkle
import Strategery.Strategies.AllRaces.WorkerRush
import Strategery.Strategies.Strategy
import Strategery.Strategies.Zerg._

import scala.util.Random

object StrategySelectionShowmatch extends StrategySelectionPolicy {
  def chooseBest(topLevelStrategies: Iterable[Strategy]): Iterable[Strategy] = {
    if (Sparkle.matches) {
      return Iterable(ZergSparkle)
    }
  
    var allowed: Vector[Strategy] = topLevelStrategies.toVector
    val games = With.history.games.filter(_.enemyName == With.enemy.name)
  
    if (With.enemy.isProtoss) {
      if (games.isEmpty) {
        return Iterable(ZvPNinePool)
      }
      if (games.size == 2) {
        return Iterable(ZvPHydraRush)
      }
      if (games.size == 3) {
        return Iterable(ZvPThirteenPoolMuta)
      }
      if (games.size == 5) {
        return Iterable(ZvPNinePool)
      }
      if (games.size == 6) {
        return Iterable(ZvPHydraRush)
      }
      return Random.shuffle(Iterable(ZvPHydraRush, ZvPNinePool, ZvPThirteenPoolMuta, ZvE4Pool, NinePoolMuta, WorkerRush)).headOption
    }
  
    val output = StrategySelectionFree.chooseBest(allowed)
    output
  }
}
