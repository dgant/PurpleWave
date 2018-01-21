package Strategery

import Lifecycle.With
import Mathematics.PurpleMath
import Performance.Cache
import Planning.Plan
import Planning.Plans.StandardGamePlan
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
  
  def selectedCurrently: Set[Strategy] = selectedCurrentlyCache()
  private val selectedCurrentlyCache = new Cache(() => selectedInitially.filter(isAppropriate))
  
  // Plasma is so weird we need to handle it separately.
  lazy val isPlasma: Boolean = With.game.mapFileName.contains("Plasma")
  lazy val isIslandMap: Boolean = heyIsThisAnIslandMap
  lazy val isFfa: Boolean = heyIsThisFFA
  
  lazy val gameplan: Plan = selectedInitially
    .find(_.gameplan.isDefined)
    .map(_.gameplan.get)
    .getOrElse(new StandardGamePlan)
  
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
      TerranChoices.tvr ++ ProtossChoices.pvr ++ ZergChoices.all
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
    lazy val ourRace                  = With.self.raceInitial
    lazy val enemyRacesCurrent        = With.enemies.map(_.raceCurrent).toSet
    lazy val enemyRaceWasUnknown      = With.enemies.exists(_.raceInitial == Race.Unknown)
    lazy val enemyRaceStillUnknown    = With.enemies.exists(_.raceCurrent == Race.Unknown)
    lazy val gamesVsEnemy             = With.history.games.count(game => With.enemies.exists(_.name == game.enemyName))
    lazy val playedEnemyOftenEnough   = gamesVsEnemy >= strategy.minimumGamesVsOpponent
    lazy val isIsland                 = isIslandMap
    lazy val isGround                 = ! isIsland
    lazy val startLocations           = With.geography.startLocations.size
    lazy val disabledInPlaybook       = Playbook.disabled.contains(strategy)
    lazy val disabledOnMap            = strategy.prohibitedMaps.exists(_.matches) || (strategy.requiredMaps.nonEmpty && ! strategy.requiredMaps.exists(_.matches))
    lazy val appropriateForOurRace    = strategy.ourRaces.exists(_ == ourRace)
    lazy val appropriateForEnemyRace  = strategy.enemyRaces.exists(race => if (race == Race.Unknown) enemyRaceWasUnknown else (enemyRaceStillUnknown || enemyRacesCurrent.contains(race)))
    lazy val appropriateForOpponent   = strategy.restrictedOpponents.isEmpty ||
      strategy.restrictedOpponents.get
        .map(_.toLowerCase)
        .exists(key => With.enemies.map(_.name.toLowerCase).exists(_.contains(key)))
    
    val output = (
          ! disabledOnMap
      &&  ! disabledInPlaybook
      &&  (strategy.ffa == isFfa)
      &&  (strategy.islandMaps  || ! isIsland)
      &&  (strategy.groundMaps  || ! isGround)
      &&  strategy.startLocationsMin <= startLocations
      &&  strategy.startLocationsMax >= startLocations
      &&  appropriateForOurRace
      &&  appropriateForEnemyRace
      &&  appropriateForOpponent
      &&  playedEnemyOftenEnough
    )
    
    output
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

  val evaluations = new mutable.HashMap[Strategy, StrategyEvaluation]
  var permutationInterest: Map[Iterable[Strategy], Double] = Map.empty
  
  def evaluate(strategy: Strategy): StrategyEvaluation = {
    if ( ! evaluations.contains(strategy)) {
      evaluations.put(strategy, StrategyEvaluation(strategy))
      strategy.choices.flatMap(choices => filterForcedStrategies(choices.filter(isAppropriate))).foreach(evaluate)
    }
    evaluations(strategy)
  }
  
  private def chooseBest(topLevelStrategies: Iterable[Strategy]): Iterable[Strategy] = {
    val permutations            = topLevelStrategies.flatMap(expandStrategy)
    val strategies              = permutations.flatten.toVector.distinct
    val strategyEvaluations     = strategies.map(strategy => (strategy, evaluate(strategy))).toMap
    val permutationEvaluations  = permutations.map(p => (p, p.map(strategyEvaluations))).toMap
        permutationInterest     = permutationEvaluations.map(p => (p._1, PurpleMath.geometricMean(p._2.map(_.interestTotal))))
    val mostInterest            = permutationInterest.values.max
    val bestPermutation         = permutationInterest.find(_._2 >= mostInterest).get
    bestPermutation._1
  }
  
  private def filterStrategies(strategies: Iterable[Strategy]): Iterable[Strategy] = {
    filterForcedStrategies(strategies.filter(isAppropriate))
  }
  
  private def expandStrategy(strategy: Strategy): Iterable[Iterable[Strategy]] = {
    
    /*
    1. Start:
    Groups of(Pick one of these strategies)
    S:[[ABC],[DEF],[GHI],[JKL]
    
    2. Filter illegal choices I, JKL
    Groups of(Pick one of these legal strategies)
    S:[[ABC],[DEF],[GH],[]]
    
    3. Remove empty choice groups
    Non-empty groups of (Pick one of these legal strategies)
    S:
    [
      [ABC],
      [DEF],
      [GH]
    ]
    
    4. Replace all choices with their expanded counterparts
    Non-empty groups of(Pick one of these(Chain of strategies)))
    S:
    [
      [
        [A,A01,A11],
        [A,A02,A11],
        ...,
        [B,B01,B11],
        [B,B02,B11],
        ...
      ],
      [
        D...
      ]
    ]
    
    5. For each row of choices, choose one of each chain
    */
    
    // 1. Groups of(Pick one of these strategies)
    
    val choices = strategy.choices
    
    // 2. Groups of(Pick one of these legal strategies)
    val legalChoices = choices.map(filterStrategies)
    
    //3. Non-empty groups of (Pick one of these legal strategies)
    val extantChoices = legalChoices.filter(_.nonEmpty)
    if (extantChoices.isEmpty) {
      return Iterable(Iterable(strategy))
    }
    
    //4. Non-empty groups of(Pick one of these(Chain of strategies)))
    val extantChoicesChains = extantChoices.map(_.flatMap(expandStrategy))
    
    var output = Iterable(Iterable(strategy))
    extantChoicesChains.foreach(nextChoiceChain =>
      output = output.flatMap(outputChain =>
        nextChoiceChain.map(nextChoice =>
          outputChain ++ nextChoice))
    )
    
    output = output.map(_.toSet.toIterable)
    output
  }
}

