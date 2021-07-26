package Strategery

import Lifecycle.With
import Mathematics.Maff
import Planning.Plan
import Planning.Plans.GamePlans.StandardGamePlan
import ProxyBwapi.Players.Players
import Strategery.History.HistoricalGame
import Strategery.Selection.{ExpandStrategy, StrategySelectionFixed, WinProbability}
import Strategery.Strategies.{AllChoices, Strategy}
import bwapi.Race

import scala.collection.mutable
import scala.util.Random

class Strategist {

  lazy val map: Option[StarCraftMap] = StarCraftMaps.all.find(_.matches)
  lazy val selectedInitially: Set[Strategy] = With.configuration.playbook.policy.chooseBranch.toSet

  private var _lastEnemyRace  = With.enemy.raceInitial
  private val _selected       = new mutable.HashSet[Strategy]
  private val _deselected     = new mutable.HashSet[Strategy]
  private val _active         = new mutable.HashSet[Strategy]

  def selected: mutable.Set[Strategy] = {
    val enemyRaceNow = With.enemy.raceCurrent
    if (_selected.isEmpty) {
      _selected ++= selectedInitially
    } else if (_lastEnemyRace != enemyRaceNow) {
      _selected.filter(_.enemyRaces.exists(r => r != Race.Unknown && r != enemyRaceNow)).foreach(swapOut)
    }
    _lastEnemyRace = enemyRaceNow
    _selected
  }
  def deselected: Iterator[Strategy] = _deselected.iterator
  def isActive(strategy: Strategy): Boolean = selected.contains(strategy) && _active.contains(strategy)
  def activate(strategy: Strategy): Boolean = {
    val output = selected.contains(strategy)
    if (output && ! _active.contains(strategy)) {
      With.logger.debug(f"Activating strategy $strategy")
      _active += strategy
    }
    output
  }
  def deactivate(strategy: Strategy): Unit = {
    if (_active.contains(strategy)) {
      With.logger.debug(f"Deactivating strategy $strategy")
    }
    _active -= strategy
  }
  def swapIn(strategy: Strategy): Unit = {
    if ( ! selected.contains(strategy)) {
      With.logger.debug(f"Swapping in strategy $strategy")
      _selected += strategy
      _deselected -= strategy
    }
  }
  def swapOut(strategy: Strategy): Unit = {
    if (selected.contains(strategy)) {
      With.logger.debug(f"Swapping out strategy $strategy")
      _selected -= strategy
      _deselected += strategy
    }
    deactivate(strategy)
  }

  private lazy val _standardGamePlan = new StandardGamePlan
  def gameplan: Plan = selected.find(_.gameplan.isDefined).map(_.gameplan.get).getOrElse(_standardGamePlan)

  lazy val heightMain       : Double  = With.self.startTile.altitude
  lazy val heightNatural    : Double  = With.geography.ourNatural.townHallTile.altitude
  lazy val isRamped         : Boolean = heightMain > heightNatural
  lazy val isFlat           : Boolean = heightMain == heightNatural
  lazy val isInverted       : Boolean = heightMain < heightNatural
  lazy val isFixedOpponent  : Boolean = With.configuration.playbook.policy.isInstanceOf[StrategySelectionFixed]
  lazy val rushDistanceMean : Double  = Maff.mean(With.geography.rushDistances)
  lazy val isIslandMap      : Boolean = With.geography.startBases.forall(base1 => With.geography.startBases.forall(base2 => base1 == base2 || With.paths.zonePath(base1.zone, base2.zone).isEmpty))
  lazy val isFfa            : Boolean = With.enemies.size > 1 && ! Players.all.exists(p => p.isAlly)
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
}

