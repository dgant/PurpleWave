package Strategery

import Lifecycle.With
import Strategery.Strategies.Strategy
import bwapi.Race

class StrategyLegality(strategy: Strategy) {
   def allowedGivenOpponentHistory(strategy: Strategy): Boolean = {
    if (strategy.responsesBlacklisted.map(_.toString).exists(With.strategy.enemyRecentFingerprints.contains)) return false
    if (strategy.responsesWhitelisted.nonEmpty
      && ! strategy.responsesWhitelisted.map(_.toString).exists(With.strategy.enemyRecentFingerprints.contains)) return false
    true
  }

  def formatName(name: String): String = name.toLowerCase.replaceAllLiterally(" ", "")

  def nameMatches(a: String, b: String): Boolean = {
    formatName(a).contains(formatName(b)) || formatName(b).contains(formatName(a))
  }

  val ourRace                 = With.self.raceInitial
  val enemyRacesCurrent       = With.enemies.map(_.raceCurrent).toSet
  val enemyRaceWasUnknown     = With.enemies.exists(_.raceInitial == Race.Unknown)
  val enemyRaceStillUnknown   = With.enemies.exists(_.raceCurrent == Race.Unknown)
  val gamesVsEnemy            = With.history.gamesVsEnemies.size
  val playedEnemyOftenEnough  = gamesVsEnemy >= strategy.minimumGamesVsOpponent
  val isIsland                = With.strategy.isIslandMap
  val isGround                = ! isIsland
  val rampOkay                = (strategy.entranceInverted || ! With.strategy.isInverted) && (strategy.entranceFlat || ! With.strategy.isFlat) && (strategy.entranceRamped || ! With.strategy.isRamped)
  val rushOkay                = With.strategy.rushDistanceMean > strategy.rushDistanceMinimum && With.strategy.rushDistanceMean < strategy.rushDistanceMaximum
  val startLocations          = With.geography.startLocations.size
  val disabledInPlaybook      = With.configuration.playbook.disabled.contains(strategy)
  val disabledOnMap           = strategy.mapsBlacklisted.exists(_.matches) || ! strategy.mapsWhitelisted.forall(_.exists(_.matches))
  val appropriateForOurRace   = strategy.ourRaces.exists(ourRace==)
  val appropriateForEnemyRace = strategy.enemyRaces.exists(race => if (race == Race.Unknown) enemyRaceWasUnknown else (enemyRaceStillUnknown || enemyRacesCurrent.contains(race)))
  val allowedGivenHumanity    = strategy.allowedVsHuman || ! With.configuration.humanMode
  val allowedGivenHistory     = allowedGivenOpponentHistory(strategy)

  val isLegal = (
    (strategy.ffa == With.strategy.isFfa)
    &&  (strategy.islandMaps  || ! isIsland)
    &&  (strategy.groundMaps  || ! isGround)
    &&  ! disabledInPlaybook
    &&  appropriateForOurRace
    &&  appropriateForEnemyRace
    &&  ( ! With.configuration.playbook.respectMap || ! disabledOnMap)
    &&  ( ! With.configuration.playbook.respectMap || strategy.startLocationsMin <= startLocations)
    &&  ( ! With.configuration.playbook.respectMap || strategy.startLocationsMax >= startLocations)
    &&  ( ! With.configuration.playbook.respectMap || rampOkay)
    &&  ( ! With.configuration.playbook.respectMap || rushOkay)
    &&  ( ! With.configuration.playbook.respectHistory || allowedGivenHistory)
    &&  ( ! With.configuration.playbook.respectHistory || playedEnemyOftenEnough)
  )
}
