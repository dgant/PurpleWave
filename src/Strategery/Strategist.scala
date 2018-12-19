package Strategery

import Lifecycle.With
import Mathematics.PurpleMath
import Planning.Plan
import Planning.Plans.GamePlans.StandardGamePlan
import ProxyBwapi.Players.Players
import Strategery.History.HistoricalGame
import Strategery.Strategies.Protoss.ProtossChoices
import Strategery.Strategies.Strategy
import Strategery.Strategies.Terran.TerranChoices
import Strategery.Strategies.Zerg.ZergChoices
import bwapi.Race

import scala.collection.mutable

class Strategist {
    
  lazy val selectedInitially: Set[Strategy] = selectInitialStrategies
  
  lazy val map: Option[StarCraftMap] = StarCraftMaps.all.find(_.matches)
   
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
  
  lazy val gameWeights: Map[HistoricalGame, Double] = With.history.games.map(game => (
    game,
    1.0 / (1.0 + (game.order / With.configuration.historyHalfLife))
  )).toMap

  lazy val enemyRecentFingerprints: Vector[String] = {
    With.history.gamesVsEnemies
      .toVector
      .sortBy(-_.timestamp)
      .take(With.configuration.recentFingerprints)
      .flatMap(_.strategies.toVector)
      .filter(_.startsWith("Finger"))
      .distinct
  }

  def selectInitialStrategies: Set[Strategy] = {
    val enemyHasKnownRace = With.enemies.exists(_.raceInitial != Race.Unknown)
    val strategiesUnfiltered = if (enemyHasKnownRace) {
      TerranChoices.all ++ ProtossChoices.all ++ ZergChoices.all
    }
    else {
      TerranChoices.tvr ++ ProtossChoices.pvr ++ ZergChoices.all
    }
    val strategiesFiltered = filterForcedStrategies(strategiesUnfiltered.filter(isAppropriate))
    strategiesFiltered.foreach(evaluate)
    Playbook.strategySelectionPolicy.chooseBest(strategiesFiltered).toSet
  }

  private def filterForcedStrategies(strategies: Iterable[Strategy]): Iterable[Strategy] = {
    if (strategies.exists(Playbook.forced.contains))
      strategies.filter(Playbook.forced.contains)
    else
      strategies
  }

  def isAppropriate(strategy: Strategy): Boolean = {
    lazy val ourRace                  = With.self.raceInitial
    lazy val enemyRacesCurrent        = With.enemies.map(_.raceCurrent).toSet
    lazy val enemyRaceWasUnknown      = With.enemies.exists(_.raceInitial == Race.Unknown)
    lazy val enemyRaceStillUnknown    = With.enemies.exists(_.raceCurrent == Race.Unknown)
    lazy val gamesVsEnemy             = With.history.gamesVsEnemies.size
    lazy val playedEnemyOftenEnough   = gamesVsEnemy >= strategy.minimumGamesVsOpponent
    lazy val isIsland                 = isIslandMap
    lazy val isGround                 = ! isIsland
    lazy val rampOkay                 = (strategy.entranceInverted || ! isInverted) && (strategy.entranceFlat || ! isFlat) && (strategy.entranceRamped || ! isRamped)
    lazy val rushOkay                 = rushDistanceMean > strategy.rushDistanceMinimum && rushDistanceMean < strategy.rushDistanceMaximum
    lazy val startLocations           = With.geography.startLocations.size
    lazy val disabledInPlaybook       = Playbook.disabled.contains(strategy)
    lazy val disabledOnMap            = strategy.mapsBlacklisted.exists(_.matches) || ! strategy.mapsWhitelisted.forall(_.exists(_.matches))
    lazy val appropriateForOurRace    = strategy.ourRaces.exists(_ == ourRace)
    lazy val appropriateForEnemyRace  = strategy.enemyRaces.exists(race => if (race == Race.Unknown) enemyRaceWasUnknown else (enemyRaceStillUnknown || enemyRacesCurrent.contains(race)))
    lazy val allowedGivenHistory      = ! strategy.responsesBlacklisted.map(_.toString).exists(enemyRecentFingerprints.contains)
    lazy val allowedForOpponent       = strategy.opponentsWhitelisted.forall(_
      .map(formatName)
      .exists(name =>
        nameMatches(name, Playbook.enemyName)
        || With.enemies.map(e => formatName(e.name)).exists(nameMatches(_, name))))

    val output = (
          ! disabledOnMap
      &&  ! disabledInPlaybook
      &&  (strategy.ffa == isFfa)
      &&  (strategy.islandMaps  || ! isIsland)
      &&  (strategy.groundMaps  || ! isGround)
      &&  strategy.startLocationsMin <= startLocations
      &&  strategy.startLocationsMax >= startLocations
      &&  rampOkay
      &&  rushOkay
      &&  appropriateForOurRace
      &&  appropriateForEnemyRace
      &&  allowedGivenHistory
      &&  allowedForOpponent
      &&  playedEnemyOftenEnough
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

