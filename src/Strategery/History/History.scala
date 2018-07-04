package Strategery.History

import Lifecycle.With
import Strategery.Playbook
import Utilities.CountMap
import bwapi.Race

import scala.collection.mutable

class History {
  
  lazy val games: Iterable[HistoricalGame] = HistoryLoader.load()
  
  lazy val currentMapName   : String  = With.game.mapFileName
  lazy val currentStarts    : Int     = With.game.getStartLocations.size
  lazy val currentEnemyName : String  = Playbook.enemyName
  lazy val currentEnemyRace : Race    = With.enemy.raceInitial
  
  var message = new mutable.ArrayBuffer[String]
  def onStart() {
    message += " "
    message += " "
    message += "Good luck on " + currentMapName + ", " + currentEnemyName + "!"
    message += " "
    
    val mapWins         = games.count(g => g.mapName        == currentMapName         &&    g.won)
    val mapLosses       = games.count(g => g.mapName        == currentMapName         &&  ! g.won)
    val startWins       = games.count(g => g.startLocations == currentStarts          &&    g.won)
    val startLosses     = games.count(g => g.startLocations == currentStarts          &&  ! g.won)
    val enemyRaceWins   = games.count(g => g.enemyRace      == currentEnemyRace       &&    g.won)
    val enemyRaceLosses = games.count(g => g.enemyRace      == currentEnemyRace       &&  ! g.won)
    val ourRaceWins     = games.count(g => g.ourRace        == With.self.raceInitial  &&    g.won)
    val ourRaceLosses   = games.count(g => g.ourRace        == With.self.raceInitial  &&  ! g.won)
    val vsWins          = games.count(g => g.enemyName      == currentEnemyName       &&    g.won)
    val vsLosses        = games.count(g => g.enemyName      == currentEnemyName       &&  ! g.won)
    
    message += "On this map: "                                         + mapWins       + " - " + mapLosses
    message += "With "  + currentStarts         + " start locations: "  + startWins     + " - " + startLosses
    message += "As "    + With.self.raceInitial + ": "                  + ourRaceWins   + " - " + ourRaceLosses
    message += "Vs. "   + currentEnemyRace      + ": "                  + enemyRaceWins + " - " + enemyRaceLosses
    message += "Vs. "   + currentEnemyName      + ": "                  + vsWins        + " - " + vsLosses
  }
  
  def onEnd(weWon: Boolean) {
    val thisGame = HistoricalGame(
      timestamp       = System.currentTimeMillis,
      startLocations  = With.geography.startLocations.size,
      mapName         = currentMapName,
      enemyName       = currentEnemyName,
      ourRace         = With.self.raceInitial,
      enemyRace       = currentEnemyRace,
      won             = weWon,
      strategies      = With.strategy.selectedCurrently.map(_.toString))
    HistoryLoader.save(games.toVector :+ thisGame)
  }
  
  def getMapHistory(mapName: String): ContextualHistory = {
    getHistory(games.filter(_.mapName == mapName))
  }
  
  def getEnemyHistory(opponentName: String): ContextualHistory = {
    getHistory(games.filter(_.enemyName == opponentName))
  }
    
  private def getHistory(matchingGames: Iterable[HistoricalGame]): ContextualHistory = {
    val wins        = matchingGames.filter(_.won)
    val losses      = matchingGames.filterNot(_.won)
    val strategies  = matchingGames.flatMap(_.strategies).toSet
    val output      = ContextualHistory(
      countByStrategy(strategies, wins),
      countByStrategy(strategies, losses))
    output
  }
  
  private def countByStrategy(
    allStrategies : Set[String],
    games         : Iterable[HistoricalGame])
      : CountMap[String] = {
    val output = new CountMap[String]
    games.foreach(game => game.strategies.foreach(output(_) += 1))
    output
  }
}
