package Strategery

import Lifecycle.With
import Strategery.Strategies.Strategy
import bwapi.Race

class StrategyLegality(strategy: Strategy) {
   def allowedGivenOpponentHistory(strategy: Strategy): Boolean = {
    if (strategy.responsesBlacklisted.map(_.toString).exists(With.strategy.enemyRecentFingerprints.contains)) return false
    if (strategy.responsesWhitelisted.nonEmpty && ! strategy.responsesWhitelisted.map(_.toString).exists(With.strategy.enemyRecentFingerprints.contains)) return false
    true
  }

  def formatName(name: String): String = name.toLowerCase.replaceAllLiterally(" ", "")

  def nameMatches(a: String, b: String): Boolean = {
    formatName(a).contains(formatName(b)) || formatName(b).contains(formatName(a))
  }

  val ourRace                 : Race = With.self.raceInitial
  val enemyRacesCurrent       : Set[Race] = With.enemies.map(_.raceCurrent).toSet
  val enemyRaceWasUnknown     : Boolean = With.enemies.exists(_.raceInitial == Race.Unknown)
  val enemyRaceStillUnknown   : Boolean = With.enemies.exists(_.raceCurrent == Race.Unknown)
  val gamesVsEnemy            : Int = With.history.gamesVsEnemies.size
  val playedEnemyOftenEnough  : Boolean = gamesVsEnemy >= strategy.minimumGamesVsOpponent
  val isIsland                : Boolean = With.strategy.isIslandMap
  val isGround                : Boolean = ! isIsland
  val rampOkay                : Boolean = (strategy.entranceInverted || ! With.strategy.isInverted) && (strategy.entranceFlat || ! With.strategy.isFlat) && (strategy.entranceRamped || ! With.strategy.isRamped)
  val rushOkay                : Boolean = With.strategy.rushDistanceMean > strategy.rushTilesMinimum && With.strategy.rushDistanceMean < strategy.rushTilesMaximum
  val startLocations          : Int = With.geography.startLocations.size
  val disabledInPlaybook      : Boolean = With.configuration.playbook.disabled.contains(strategy)
  val disabledOnMap           : Boolean = strategy.mapsBlacklisted.exists(_()) || (strategy.mapsWhitelisted.nonEmpty && ! strategy.mapsWhitelisted.exists(_()))
  val appropriateForOurRace   : Boolean = strategy.ourRaces.exists(ourRace==)
  val appropriateForEnemyRace : Boolean = strategy.enemyRaces.exists(race => if (race == Race.Unknown) enemyRaceWasUnknown else (enemyRaceStillUnknown || enemyRacesCurrent.contains(race)))
  val allowedGivenHumanity    : Boolean = strategy.allowedVsHuman || ! With.configuration.humanMode
  val allowedGivenHistory     : Boolean = allowedGivenOpponentHistory(strategy)
  val allowedGivenRequirements: Boolean = strategy.requirements.forall(_())

  val isLegal: Boolean = (
    (strategy.ffa == With.strategy.isFfa)
    &&  (strategy.islandMaps  || ! isIsland)
    &&  (strategy.groundMaps  || ! isGround)
    &&  ! disabledInPlaybook
    &&  appropriateForOurRace
    &&  appropriateForEnemyRace
    &&  allowedGivenRequirements
    &&  ( ! With.configuration.playbook.respectMap || ! disabledOnMap)
    &&  ( ! With.configuration.playbook.respectMap || strategy.startLocationsMin <= startLocations)
    &&  ( ! With.configuration.playbook.respectMap || strategy.startLocationsMax >= startLocations)
    &&  ( ! With.configuration.playbook.respectMap || rampOkay)
    &&  ( ! With.configuration.playbook.respectMap || rushOkay)
    &&  ( ! With.configuration.playbook.respectHistory || allowedGivenHistory)
    &&  ( ! With.configuration.playbook.respectHistory || playedEnemyOftenEnough)
  )
}
