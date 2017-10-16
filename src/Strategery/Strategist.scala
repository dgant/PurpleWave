package Strategery

import Lifecycle.With
import Performance.Cache
import Planning.Plan
import Planning.Plans.WinTheGame
import Strategery.History.HistoricalGame
import Strategery.Strategies.Protoss.ProtossChoices
import Strategery.Strategies.Strategy
import Strategery.Strategies.Terran.TerranChoices
import Strategery.Strategies.Zerg.ZergChoices
import bwapi.Race

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Strategist {
  
  lazy val selectedInitially: Set[Strategy] = selectInitialStrategies
  
  def selectedCurrently: Set[Strategy] = selectedCurrentlyCache()
  private val selectedCurrentlyCache = new Cache(() => selectedInitially.filter(isAppropriate))
  
  // Plasma is so weird we need to handle it separately.
  lazy val isPlasma: Boolean = With.game.mapFileName.contains("Plasma")
  
  lazy val isIslandMap: Boolean = heyIsThisAnIslandMap
  
  lazy val gameplan: Plan = selectedInitially
    .find(_.gameplan.isDefined)
    .map(_.gameplan.get)
    .getOrElse(new WinTheGame)
  
  lazy val gameWeights: Map[HistoricalGame, Double] = With.history.games.map(game => (
    game,
    1.0 / (1.0 + (game.order / With.configuration.historyHalfLife))
  )).toMap
  
  def selectInitialStrategies: Set[Strategy] = {
    val enemyHasKnownRace = With.enemies.exists(_.raceInitial != Race.Unknown)
    val strategiesUnfiltered = if (enemyHasKnownRace) {
      TerranChoices.all ++ ProtossChoices.all ++ ZergChoices.all
    }
    else {
      TerranChoices.all ++ ProtossChoices.pvr ++ ZergChoices.all
    }
    val strategiesFiltered = filterForcedStrategies(strategiesUnfiltered.filter(isAppropriate))
    strategiesFiltered.foreach(evaluate)
    chooseBest(strategiesFiltered).toSet
  }
  
  private def filterForcedStrategies(strategies: Iterable[Strategy]): Iterable[Strategy] = {
    if (strategies.exists(Playbook.forced.contains))
      strategies.filter(Playbook.forced.contains)
    else
      strategies
  }
  
  private def isAppropriate(strategy: Strategy): Boolean = {
    lazy val ourRace                = With.self.raceInitial
    lazy val enemyRacesCurrent      = With.enemies.map(_.raceCurrent).toSet
    lazy val enemyIsRandom          = With.enemies.exists(_.raceInitial == Race.Unknown)
    lazy val isIsland               = isIslandMap
    lazy val isGround               = ! isIsland
    lazy val startLocations         = With.geography.startLocations.size
    lazy val thisGameIsFFA          = With.enemies.size > 1
    lazy val disabledInPlaybook     = Playbook.disabled.contains(strategy)
    lazy val disabledOnMap          = strategy.prohibitedMaps.exists(_.matches)
    lazy val appropriateForRace     = enemyIsRandom || enemyRacesCurrent.exists(race => strategy.ourRaces.exists(_ == race))
    lazy val appropriateForOpponent = strategy.restrictedOpponents.isEmpty ||
      strategy.restrictedOpponents.get
        .map(_.toLowerCase)
        .exists(key => With.enemies.map(_.name.toLowerCase).exists(_.contains(key)))
    
    ! disabledOnMap                                   &&
    ! disabledInPlaybook                              &&
    (strategy.ffa == thisGameIsFFA)                   &&
    (strategy.islandMaps  || ! isIsland)              &&
    (strategy.groundMaps  || ! isGround)              &&
    strategy.ourRaces.exists(_ == ourRace)            &&
    strategy.startLocationsMin <= startLocations      &&
    strategy.startLocationsMax >= startLocations      &&
    appropriateForRace                                &&
    appropriateForOpponent
  }
  
  private def heyIsThisAnIslandMap = {
    isPlasma ||
      With.geography.startBases.forall(base1 =>
        With.geography.startBases.forall(base2 =>
          base1 == base2 || With.paths.zonePath(base1.zone, base2.zone).isEmpty))
  }

  val evaluations = new mutable.HashMap[Strategy, StrategyEvaluation]
  
  def evaluate(strategy: Strategy): StrategyEvaluation = {
    if ( ! evaluations.contains(strategy)) {
      evaluations.put(strategy, StrategyEvaluation(strategy))
      strategy.choices.flatMap(choices => filterForcedStrategies(choices.filter(isAppropriate))).foreach(evaluate)
    }
    evaluations(strategy)
  }
  
  private def chooseBest(strategies: Iterable[Strategy]): Iterable[Strategy] = {
    if (strategies.isEmpty) {
      return Iterable.empty
    }
    
    val output        = new ArrayBuffer[Strategy]
    val evaluated     = strategies.map(evaluate)
    val bestVsEnemy   = evaluated.map(_.winrateVsEnemy).max
    val untested      = evaluated.filter(_.games.isEmpty)
    
    val bestEvaluation = evaluated.toSeq.sortBy(_.playbookOrder).maxBy(_.interestTotal)
      /*
      if (bestVsEnemy >= With.configuration.rideItOutWinrate) {
        evaluated
          .filter(_.winrateVsEnemy >= bestVsEnemy)
          .toVector
          .minBy(_.playbookOrder)
      }
      else if (untested.nonEmpty) {
        untested.minBy(_.playbookOrder)
      }
      else {
        evaluated.maxBy(_.interestTotal)
      }*/
    
    val bestStrategy = bestEvaluation.strategy
    output.append(bestStrategy)
    output ++= bestStrategy.choices.flatMap(choice => chooseBest(filterForcedStrategies(choice.filter(isAppropriate))))
    output
  }
}

