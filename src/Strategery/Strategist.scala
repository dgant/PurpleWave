package Strategery

import Lifecycle.With
import Planning.Plan
import Planning.Plans.WinTheGame
import Strategery.Strategies.{AllStrategies, Strategy}

import scala.util.Random

class Strategist {
  
  lazy val selected: Set[Strategy] = selectStrategies
  
  lazy val gameplan: Plan = selected
    .find(_.gameplan.isDefined)
    .map(_.gameplan.get)
    .getOrElse(new WinTheGame)
  
  def selectStrategies: Set[Strategy] = {
    val ourRace = With.self.race
    val enemyRaces = With.enemies.map(_.race).toSet
    val isIsland =
      With.geography.startLocations.forall(start1 =>
        With.geography.startLocations.forall(start2 =>
          ! With.paths.exists(start1, start2)))
    val isGround = ! isIsland
    val startLocations = With.geography.startLocations.size
    
    val availableStrategies = AllStrategies.values
      .filter(strategy =>
        (strategy.islandMaps  || ! isIsland) &&
        (strategy.groundMaps  || ! isGround) &&
        strategy.ourRaces.exists(_ == ourRace) &&
        strategy.enemyRaces.exists(enemyRaces.contains) &&
        strategy.startLocationsMin <= startLocations &&
        strategy.startLocationsMax >= startLocations)
    
    // TODO: Replace with actual selection
    Random.shuffle(availableStrategies).take(1).toSet
  }
}

