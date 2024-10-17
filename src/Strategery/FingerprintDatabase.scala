package Strategery

import Lifecycle.With
import Utilities.?
import bwapi.Race

trait FingerprintDatabase {
  private lazy val recentGamesCheckingForMultipleRaces = With.history.gamesVsEnemies.take(10)
  private lazy val enemyHasBeenTerran   : Int = Math.min(1, recentGamesCheckingForMultipleRaces.count(_.enemyRace == Race.Terran))
  private lazy val enemyHasBeenProtoss  : Int = Math.min(1, recentGamesCheckingForMultipleRaces.count(_.enemyRace == Race.Protoss))
  private lazy val enemyHasBeenZerg     : Int = Math.min(1, recentGamesCheckingForMultipleRaces.count(_.enemyRace == Race.Zerg))
  private lazy val enemyMultipleRaces   : Boolean = (enemyHasBeenTerran + enemyHasBeenProtoss + enemyHasBeenZerg > 1)

  lazy val enemyRecentFingerprints: Vector[String] = enemyFingerprints(With.configuration.recentFingerprints)

  def enemyFingerprints(games: Int): Vector[String] = {
    val finalGames = ?(enemyMultipleRaces, 3, 1) * games
    With.history.gamesVsEnemies.take(finalGames).flatMap(_.tags.toVector).filter(_.startsWith("Finger")).distinct
  }
}
