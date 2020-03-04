package Strategery

import Lifecycle.With
import Mathematics.PurpleMath
import Planning.Plan
import Planning.Plans.GamePlans.StandardGamePlan
import ProxyBwapi.Players.Players
import Strategery.History.HistoricalGame
import Strategery.Selection.{ExpandStrategy, WinProbability}
import Strategery.Strategies.{AllChoices, Strategy}
import bwapi.Race

class Strategist {

  lazy val map: Option[StarCraftMap] = StarCraftMaps.all.find(_.matches)
  lazy val selectedInitially: Set[Strategy] = With.configuration.playbook.strategySelectionPolicy.chooseBranch.toSet

  private var enemyRaceAtLastCheck: Race = With.enemy.raceInitial
  private var selectedLast: Option[Set[Strategy]] = None
  def selectedCurrently: Set[Strategy] = {
    val enemyRaceNow = With.enemy.raceCurrent
    if (selectedLast.isEmpty) {
      selectedLast = Some(selectedInitially)
    }
    else if (enemyRaceAtLastCheck != enemyRaceNow || With.frame < 5) { // Hack fix
      selectedLast = Some(selectedInitially.filter(_.legality.isLegal))
    }
    enemyRaceAtLastCheck = enemyRaceNow
    selectedLast.get
  }

  lazy val heightMain       : Double  = With.grids.altitudeBonus.get(With.self.startTile)
  lazy val heightNatural    : Double  = With.grids.altitudeBonus.get(With.geography.ourNatural.townHallTile)
  lazy val isRamped         : Boolean = heightMain > heightNatural
  lazy val isFlat           : Boolean = heightMain == heightNatural
  lazy val isInverted       : Boolean = heightMain < heightNatural
  lazy val rushDistanceMean : Double  = PurpleMath.mean(With.geography.rushDistances)
  lazy val isPlasma         : Boolean = Plasma.matches
  lazy val isIslandMap      : Boolean = isPlasma || With.geography.startBases.forall(base1 => With.geography.startBases.forall(base2 => base1 == base2 || With.paths.zonePath(base1.zone, base2.zone).isEmpty))
  lazy val isFfa            : Boolean = With.enemies.size > 1 && ! Players.all.exists(p => p.isAlly)
  lazy val gameplan         : Plan    = selectedInitially.find(_.gameplan.isDefined).map(_.gameplan.get).getOrElse(new StandardGamePlan)
  lazy val gameWeights: Map[HistoricalGame, Double] = With.history.games
    .filter(_.enemyMatches)
    .zipWithIndex
    .map(p => (p._1, 1.0 / (1.0 + (p._2 / With.configuration.historyHalfLife))))
    .toMap

  lazy val enemyRecentFingerprints: Vector[String] = enemyFingerprints(With.configuration.recentFingerprints)
  def enemyFingerprints(games: Int = With.configuration.recentFingerprints): Vector[String] = {
    With.history.gamesVsEnemies.take(games).flatMap(_.tags.toVector).filter(_.startsWith("Finger")).distinct
  }

  lazy val strategiesTopLevel : Seq[Strategy] = AllChoices.tree
  lazy val strategiesAll      : Seq[Strategy] = strategyBranchesUnfiltered.flatten.distinct

  lazy val strategyBranchesUnfiltered : Seq[Seq[Strategy]] = strategiesTopLevel.flatMap(ExpandStrategy.apply).distinct
  lazy val strategyBranchesLegal      : Seq[Seq[Strategy]] = strategyBranchesUnfiltered.filter(_.forall(_.legality.isLegal))

  lazy val legalities   : Map[Strategy, StrategyLegality]   = strategiesAll.distinct.map(s => (s, new StrategyLegality(s))).toMap
  lazy val evaluations  : Map[Strategy, StrategyEvaluation] = strategiesAll.distinct.map(s => (s, new StrategyEvaluation(s))).toMap

  lazy val gamesVsOpponent: Iterable[HistoricalGame] = if (With.enemies.size > 1) Iterable.empty else With.history.games.filter(_.enemyName == With.configuration.playbook.enemyName)
  lazy val winProbability: Double = if (gamesVsOpponent.isEmpty) With.configuration.targetWinrate else gamesVsOpponent.filter(_.won).map(_.weight).sum / gamesVsOpponent.map(_.weight).sum
  lazy val winProbabilityByBranch       : Map[Iterable[Strategy], Double] = strategyBranchesUnfiltered.map(b => (b, WinProbability(b))).toMap
  lazy val winProbabilityByBranchLegal  : Map[Iterable[Strategy], Double] = winProbabilityByBranch.filter(_._1.forall(_.legality.isLegal))
}

