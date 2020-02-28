package Strategery

import Lifecycle.{Manners, With}
import Mathematics.PurpleMath
import Planning.Plan
import Planning.Plans.GamePlans.StandardGamePlan
import ProxyBwapi.Players.Players
import Strategery.History.HistoricalGame
import Strategery.Selection.StrategySelectionDynamic
import Strategery.Strategies.{AllChoices, Strategy}
import bwapi.Race

import scala.collection.mutable

class Strategist {

  lazy val selectedInitially: Set[Strategy] = selectInitialStrategies

  lazy val map: Option[StarCraftMap] = StarCraftMaps.all.find(_.matches)

  private def playbook: Playbook = With.configuration.playbook

  private var enemyRaceAtLastCheck: Race = With.enemy.raceInitial
  private var selectedLast: Option[Set[Strategy]] = None

  def selectedCurrently: Set[Strategy] = {
    val enemyRaceNow = With.enemy.raceCurrent
    if (selectedLast.isEmpty) {
      selectedLast = Some(selectedInitially)
    }
    else if (enemyRaceAtLastCheck != enemyRaceNow || With.frame < 5) { // Hack fix
      selectedLast = Some(selectedInitially.filter(isAppropriate))
    }
    enemyRaceAtLastCheck = enemyRaceNow
    selectedLast.get
  }

  // Plasma is so weird we need to handle it separately.
  lazy val isPlasma: Boolean = Plasma.matches
  lazy val isIslandMap: Boolean = heyIsThisAnIslandMap
  lazy val isFfa: Boolean = heyIsThisFFA

  private lazy val heightMain = With.grids.altitudeBonus.get(With.self.startTile)
  private lazy val heightNatural = With.grids.altitudeBonus.get(With.geography.ourNatural.townHallTile)
  lazy val isRamped: Boolean = heightMain > heightNatural
  lazy val isFlat: Boolean = heightMain == heightNatural
  lazy val isInverted: Boolean = heightMain < heightNatural
  lazy val rushDistanceMean: Double = PurpleMath.mean(With.geography.rushDistances)

  lazy val gameplan: Plan = selectedInitially
    .find(_.gameplan.isDefined)
    .map(_.gameplan.get)
    .getOrElse(new StandardGamePlan)

  lazy val gameWeights: Map[HistoricalGame, Double] = With.history.games
    .filter(_.enemyMatches)
    .zipWithIndex
    .toVector
    .map { case (game: HistoricalGame, i: Int) => (
      game,
      1.0 / (1.0 + (i / With.configuration.historyHalfLife))
    )
    }
    .toMap

  def enemyFingerprints(games: Int = With.configuration.recentFingerprints): Vector[String] = {
    With.history.gamesVsEnemies
      .take(games)
      .flatMap(_.strategies.toVector)
      .filter(_.startsWith("Finger"))
      .distinct
  }

  lazy val enemyRecentFingerprints: Vector[String] = enemyFingerprints(With.configuration.recentFingerprints)

  def strategiesUnfiltered = if (With.enemies.exists(_.raceInitial == Race.Unknown)) AllChoices.treeVsRandom else AllChoices.treeVsKnownRace
  def strategiesFiltered = filterForcedStrategies(strategiesUnfiltered.filter(isAppropriate))

  def selectInitialStrategies: Set[Strategy] = {
    val strategies = strategiesFiltered
    strategies.foreach(evaluate)
    if (With.configuration.humanMode) {
      Manners.chat("Human mode enabled!")
      With.configuration.strategyRandomness = 0.3
      return StrategySelectionDynamic.chooseBest(strategies).toSet
    }
    playbook.strategySelectionPolicy.chooseBestUnfiltered(strategiesUnfiltered).getOrElse(
      playbook.strategySelectionPolicy.chooseBest(strategies))
      .toSet
  }

  private def filterForcedStrategies(strategies: Iterable[Strategy]): Iterable[Strategy] = {
    if (strategies.exists(playbook.forced.contains))
      strategies.filter(playbook.forced.contains)
    else
      strategies
  }

  def allowedGivenOpponentHistory(strategy: Strategy): Boolean = {
    if (strategy.responsesBlacklisted.map(_.toString).exists(enemyRecentFingerprints.contains)) return false
    if (strategy.responsesWhitelisted.nonEmpty
      && ! strategy.responsesWhitelisted.map(_.toString).exists(enemyRecentFingerprints.contains)) return false
    true
  }

  lazy val humanModeEnabled = With.configuration.humanMode
  def isAppropriate(strategy: Strategy): Boolean = {
    val ourRace                  = With.self.raceInitial
    val enemyRacesCurrent        = With.enemies.map(_.raceCurrent).toSet
    val enemyRaceWasUnknown      = With.enemies.exists(_.raceInitial == Race.Unknown)
    val enemyRaceStillUnknown    = With.enemies.exists(_.raceCurrent == Race.Unknown)
    val gamesVsEnemy             = With.history.gamesVsEnemies.size
    val playedEnemyOftenEnough   = gamesVsEnemy >= strategy.minimumGamesVsOpponent
    val isIsland                 = isIslandMap
    val isGround                 = ! isIsland
    val rampOkay                 = (strategy.entranceInverted || ! isInverted) && (strategy.entranceFlat || ! isFlat) && (strategy.entranceRamped || ! isRamped)
    val rushOkay                 = rushDistanceMean > strategy.rushDistanceMinimum && rushDistanceMean < strategy.rushDistanceMaximum
    val startLocations           = With.geography.startLocations.size
    val disabledInPlaybook       = playbook.disabled.contains(strategy)
    val disabledOnMap            = strategy.mapsBlacklisted.exists(_.matches) || ! strategy.mapsWhitelisted.forall(_.exists(_.matches))
    val appropriateForOurRace    = strategy.ourRaces.exists(_ == ourRace)
    val appropriateForEnemyRace  = strategy.enemyRaces.exists(race => if (race == Race.Unknown) enemyRaceWasUnknown else (enemyRaceStillUnknown || enemyRacesCurrent.contains(race)))
    val allowedGivenHumanity     = strategy.allowedVsHuman || ! humanModeEnabled
    val allowedGivenHistory      = allowedGivenOpponentHistory(strategy)
    val allowedForOpponent       = strategy.opponentsWhitelisted.forall(_
      .map(formatName)
      .exists(name =>
        nameMatches(name, playbook.enemyName)
        || With.enemies.map(e => formatName(e.name)).exists(nameMatches(_, name))))

    val output = (
      (strategy.ffa == isFfa)
      &&  (strategy.islandMaps  || ! isIsland)
      &&  (strategy.groundMaps  || ! isGround)
      &&  ! disabledInPlaybook
      &&  appropriateForOurRace
      &&  appropriateForEnemyRace
      &&  ( ! playbook.respectOpponent || allowedForOpponent)
      &&  ( ! playbook.respectMap || ! disabledOnMap)
      &&  ( ! playbook.respectMap || strategy.startLocationsMin <= startLocations)
      &&  ( ! playbook.respectMap || strategy.startLocationsMax >= startLocations)
      &&  ( ! playbook.respectMap || rampOkay)
      &&  ( ! playbook.respectMap || rushOkay)
      &&  ( ! playbook.respectHistory || allowedGivenHistory)
      &&  ( ! playbook.respectHistory || playedEnemyOftenEnough)
    )
    
    output
  }

  def formatName(name: String): String = name.toLowerCase.replaceAllLiterally(" ", "")

  def nameMatches(a: String, b: String): Boolean = {
    formatName(a).contains(formatName(b)) || formatName(b).contains(formatName(a))
  }
  
  private def heyIsThisAnIslandMap = {
    isPlasma ||
      With.geography.startBases.forall(base1 =>
        With.geography.startBases.forall(base2 =>
          base1 == base2 || With.paths.zonePath(base1.zone, base2.zone).isEmpty))
  }
  
  private def heyIsThisFFA = {
    With.enemies.size > 1 && ! Players.all.exists(p => p.isAlly)
  }
  
  def filterStrategies(strategies: Iterable[Strategy]): Iterable[Strategy] = {
    filterForcedStrategies(strategies.filter(isAppropriate))
  }

  val evaluations = new mutable.HashMap[Strategy, StrategyEvaluation]
  var interest: Map[Iterable[Strategy], Double] = Map.empty
  
  def evaluate(strategy: Strategy): StrategyEvaluation = {
    if ( ! evaluations.contains(strategy)) {
      evaluations.put(strategy, StrategyEvaluation(strategy))
      strategy.choices.flatMap(choices => filterForcedStrategies(choices.filter(isAppropriate))).foreach(evaluate)
    }
    evaluations(strategy)
  }
}

