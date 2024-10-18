package Strategery

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Utilities.?
import bwapi.Race

trait RecentFingerprints {
  private lazy val recentGames = With.history.gamesVsEnemies.take(10)
  private lazy val enemyHasBeenTerran   : Boolean = recentGames.exists(_.enemyRace == Race.Terran)
  private lazy val enemyHasBeenProtoss  : Boolean = recentGames.exists(_.enemyRace == Race.Protoss)
  private lazy val enemyHasBeenZerg     : Boolean = recentGames.exists(_.enemyRace == Race.Zerg)
  private lazy val enemyMultipleRaces   : Boolean = Seq(enemyHasBeenTerran, enemyHasBeenProtoss, enemyHasBeenZerg).count(_ == true) > 1

  lazy val enemyRecentFingerprints: Set[Fingerprint] = enemyFingerprints(With.configuration.recentFingerprints)

  def enemyFingerprints(games: Int): Set[Fingerprint] = {
    Tags.fingerprints(
      With.history.gamesVsEnemies
        .take(?(enemyMultipleRaces, 3, 1) * games)
        .flatMap(_.tags)).toSet
  }
}
