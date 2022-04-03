package Strategery.History

import Lifecycle.With
import Utilities.Time.Frames
import bwapi.Race

import scala.collection.mutable

class History {
  
  lazy val games: Iterable[HistoricalGame] = HistoryLoader.load()
  
  lazy val currentMapName   : String  = With.game.mapFileName
  lazy val currentStarts    : Int     = With.game.getStartLocations.size
  lazy val currentEnemyRace : Race    = With.enemy.raceInitial
  def currentEnemyName      : String  = With.configuration.playbook.enemyName
  
  var message = new mutable.ArrayBuffer[String]
  def onStart() {
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
    
    message += "On this map: "                                          + mapWins       + " - " + mapLosses
    message += "With "  + currentStarts         + " start locations: "  + startWins     + " - " + startLosses
    message += "As this race: "                                         + ourRaceWins   + " - " + ourRaceLosses
    message += "Vs. "   + currentEnemyRace      + ": "                  + enemyRaceWins + " - " + enemyRaceLosses
    message += "Vs. "   + currentEnemyName      + ": "                  + vsWins        + " - " + vsLosses

    if (With.configuration.humanMode) {
      message.clear()
    }
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
      tags = (
        Vector(Frames(With.frame).toString)
        ++ With.strategy.selected.filter(_.isActive).map(_.toString)
        ++ With.strategy.selected.filterNot(_.isActive).map(s => f"[$s]").toVector
        ++ With.fingerprints.all.filter(_()).map(_.toString).sorted).distinct)
    HistoryLoader.save(games.toVector :+ thisGame)
  }

  lazy val gamesVsEnemies: Vector[HistoricalGame] = {
    games
      .view
      .filter(game => With.enemies.exists(_.name == game.enemyName))
      .toVector
      .sortBy(-_.timestamp)
  }
}
