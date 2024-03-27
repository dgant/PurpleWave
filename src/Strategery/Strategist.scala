package Strategery

import Lifecycle.Configure.ConfigurationLoader.matchNames
import Lifecycle.With
import Mathematics.Maff
import Planning.Plan
import Planning.Plans.GamePlans.All.StandardGamePlan
import ProxyBwapi.Players.Players
import Strategery.History.HistoricalGame
import Strategery.Selection._
import Strategery.Strategies.{AllChoices, Strategy}
import bwapi.{GameType, Race}

import scala.collection.mutable
import scala.util.Random

class Strategist {

  lazy val map: Option[StarCraftMap] = StarCraftMaps.all.find(_())
  lazy val selectedInitially: Set[Strategy] = With.configuration.playbook.policy.chooseBranch.toSet

  private var _lastEnemyRace  = With.enemy.raceInitial
  val selected    = new mutable.HashSet[Strategy]
  val deselected  = new mutable.HashSet[Strategy]
  val active      = new mutable.HashSet[Strategy]

  def update(): Unit = {
    val enemyRaceNow = With.enemy.raceCurrent
    if (selected.isEmpty) {
      if (With.configuration.fixedBuilds.nonEmpty) {
        setFixedBuild(With.configuration.fixedBuilds)
      }
      selected ++= selectedInitially
    } else if (_lastEnemyRace != enemyRaceNow) {
      val toRemove = selected.filter(_.enemyRaces.forall(r => r != Race.Unknown && r != enemyRaceNow))
      toRemove.foreach(swapOut)
    }
    _lastEnemyRace = enemyRaceNow
  }

  def isActive(strategy: Strategy): Boolean = selected.contains(strategy) && active.contains(strategy)
  def isSelected(strategy: Strategy): Boolean = selected.contains(strategy)
  def activate(strategy: Strategy): Boolean = {
    // Use public "selected" to force initialization
    val output = selected.contains(strategy)
    if (output && ! active.contains(strategy)) {
      With.logger.debug(f"Activating strategy $strategy")
      active += strategy
    }
    output
  }
  def deactivate(strategy: Strategy): Unit = {
    if (active.contains(strategy)) {
      With.logger.debug(f"Deactivating strategy $strategy")
    }
    active -= strategy
  }
  def swapIn(strategy: Strategy): Unit = {
    if ( ! selected.contains(strategy)) {
      With.logger.debug(f"Swapping in strategy $strategy")
      selected += strategy
      deselected -= strategy
    }
  }
  def swapOut(strategy: Strategy): Unit = {
    if (selected.contains(strategy)) {
      With.logger.debug(f"Swapping out strategy $strategy")
      selected -= strategy
      deselected += strategy
    }
    deactivate(strategy)
  }
  def swapEverything(whitelisted: Seq[Strategy], blacklisted: Seq[Strategy] = Seq.empty): Unit = {
    val matchedBranches = Maff.orElse(
      strategyBranchesLegal     .filter(branch => whitelisted.forall(branch.contains)).filterNot(branch => blacklisted.exists(branch.contains)),
      strategyBranchesUnfiltered.filter(branch => whitelisted.forall(branch.contains)).filterNot(branch => blacklisted.exists(branch.contains)))
    val bestBranch = Maff.maxBy(matchedBranches)(branch => winProbabilityByBranch.getOrElse(branch, 0.0))
    bestBranch.foreach(branch => {
      selected.filterNot(branch.contains).foreach(_.swapOut())
      branch.filterNot(selected.contains).foreach(_.swapIn())
    })
    if (bestBranch.isEmpty) {
      With.logger.warn(f"Attempted to swap everything, but found  no legal branches. Whitelisted: $whitelisted - Blacklisted:  $blacklisted")
      blacklisted.foreach(_.swapOut())
      whitelisted.foreach(_.swapIn())
    }
  }

  private lazy val _standardGamePlan = new StandardGamePlan
  private var _lastSelectedWithGameplan: Option[Strategy] = None
  private var _lastCustomGameplan: Option[Plan] = None
  def gameplan: Plan = {
    val selectedWithGameplan = selected.find(_.gameplan.isDefined)
    if (selectedWithGameplan.isDefined) {
      selectedWithGameplan.get.activate()
      if ( ! _lastSelectedWithGameplan.contains(selectedWithGameplan.get)) {
        _lastSelectedWithGameplan = selectedWithGameplan
        _lastCustomGameplan = _lastSelectedWithGameplan.get.gameplan
      }
    } else {
      _lastSelectedWithGameplan = None
      _lastCustomGameplan = None
    }
    _lastCustomGameplan.getOrElse(_standardGamePlan)
  }

  lazy val heightMain       : Double  = With.self.startTile.altitude
  lazy val heightNatural    : Double  = With.geography.ourNatural.townHallTile.altitude
  lazy val isRamped         : Boolean = heightMain > heightNatural
  lazy val isFlat           : Boolean = heightMain == heightNatural
  lazy val isInverted       : Boolean = heightMain < heightNatural
  lazy val isFixedOpponent  : Boolean = With.configuration.playbook.policy.isInstanceOf[StrategySelectionFixed]
  lazy val rushDistanceMin  : Int     = Maff.min(With.geography.rushDistances).getOrElse(rushDistanceMean)
  lazy val rushDistanceMax  : Int     = Maff.max(With.geography.rushDistances).getOrElse(rushDistanceMean)
  lazy val rushDistanceMean : Int     = Maff.mean(With.geography.rushDistances.map(_.toDouble)).toInt
  lazy val isIslandMap      : Boolean = With.geography.mains.forall(base1 => With.geography.mains.forall(base2 => base1 == base2 || With.paths.zonePath(base1.zone, base2.zone).isEmpty))
  lazy val isFfa            : Boolean = With.enemies.size > 1 && ! Players.all.exists(p => p.isAlly) && With.game.getGameType != GameType.Top_vs_Bottom
  lazy val isMoneyMap       : Boolean = With.geography.mains.forall(b => b.minerals.length >= 16 && b.mineralsLeft > 1500 * 40)
  lazy val gameWeights: Map[HistoricalGame, Double] = With.history.games
    .filter(_.enemyMatches)
    .zipWithIndex
    .map(p => (p._1, 1.0 / (1.0 + (p._2 / With.configuration.historyHalfLife))))
    .toMap

  lazy val enemyRecentFingerprints: Vector[String] = enemyFingerprints(With.configuration.recentFingerprints)
  private lazy val recentGamesCheckingForMultipleRaces = With.history.gamesVsEnemies.take(10)
  private lazy val enemyHasBeenTerran   : Int = Math.min(1, recentGamesCheckingForMultipleRaces.count(_.enemyRace == Race.Terran))
  private lazy val enemyHasBeenProtoss  : Int = Math.min(1, recentGamesCheckingForMultipleRaces.count(_.enemyRace == Race.Protoss))
  private lazy val enemyHasBeenZerg     : Int = Math.min(1, recentGamesCheckingForMultipleRaces.count(_.enemyRace == Race.Zerg))
  private lazy val enemyMultipleRaces   : Boolean = (enemyHasBeenTerran + enemyHasBeenProtoss + enemyHasBeenZerg > 1)
  def enemyFingerprints(games: Int): Vector[String] = {
    val finalGames = (if (enemyMultipleRaces) 3 else 1) * games
    With.history.gamesVsEnemies.take(finalGames).flatMap(_.tags.toVector).filter(_.startsWith("Finger")).distinct
  }

  lazy val strategiesTopLevel : Seq[Strategy] = if (With.enemy.raceCurrent == Race.Unknown) AllChoices.treeVsRandom else AllChoices.treeVsKnownRace
  lazy val strategiesAll      : Seq[Strategy] = strategyBranchesUnfiltered.flatten.distinct

  lazy val strategyBranchesUnfiltered : Seq[Seq[Strategy]] = strategiesTopLevel.flatMap(ExpandStrategy.apply).distinct
  lazy val strategyBranchesLegal      : Seq[Seq[Strategy]] = strategyBranchesUnfiltered.filter(_.forall(_.legality.isLegal))

  lazy val legalities   : Map[Strategy, StrategyLegality]   = strategiesAll.distinct.map(s => (s, new StrategyLegality(s))).toMap
  lazy val evaluations  : Map[Strategy, StrategyEvaluation] = strategiesAll.distinct.map(s => (s, new StrategyEvaluation(s))).toMap

  lazy val gamesVsOpponent: Iterable[HistoricalGame] = if (With.enemies.size > 1) Iterable.empty else With.history.games.filter(_.enemyName == With.configuration.playbook.enemyName)
  lazy val winProbability: Double = if (gamesVsOpponent.isEmpty) With.configuration.targetWinrate else gamesVsOpponent.filter(_.won).map(_.weight).sum / gamesVsOpponent.map(_.weight).sum
  lazy val winProbabilityByBranch       : Map[Iterable[Strategy], Double] = strategyBranchesUnfiltered.map(b => (b, WinProbability(b))).toMap
  lazy val winProbabilityByBranchLegal  : Map[Iterable[Strategy], Double] = winProbabilityByBranch.filter(_._1.forall(_.legality.isLegal))

  val rolls: mutable.HashMap[String, Boolean] = new mutable.HashMap
  def roll(key: String, probability: Double): Boolean = {
    if ( ! rolls.contains(key)) {
      val rolled = Random.nextDouble()
      val success = rolled <= probability
      With.logger.debug(f"Roll for $key ${if (success) "PASSED" else "FAILED"} (Rolled $rolled into probability $probability)")
      rolls(key) = success
    }
    rolls(key)
  }

  private def setFixedBuild(strategyNamesText: String): Unit = {
    // The implementation of this is a little tricky because we have to call this before the Strategist has been instantiated

    // Get all the strategy names
    val strategyNamesLines = strategyNamesText.replaceAll(",", " ").replaceAll("  ", " ").split("[\r\n]+").filter(_.nonEmpty).toVector
    val strategyNames = Maff.sample(strategyNamesLines).split(" ")

    // Get all the mapped strategy objects
    var matchingBranches = matchNames(strategyNames, AllChoices.tree.flatMap(ExpandStrategy.apply).distinct)
    if (matchingBranches.isEmpty) {
      With.logger.warn("Tried to use fixed build but failed to match " + strategyNamesText)
    }
    if (matchingBranches.nonEmpty) {
      With.logger.debug("Using fixed build: " + strategyNamesText)
      With.configuration.forcedPlaybook = Some(new TestingPlaybook {
        override def policy: StrategySelectionPolicy = StrategySelectionGreedy(Some(matchingBranches))
      })
    }
  }
}

