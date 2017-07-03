package Strategery.History

import Lifecycle.With
import bwapi.Race

object HistorySerializer {
  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////
  //                                                                                                  //
  // Hey, are you updating this file?                                                                 //
  // Consider updating the FORMAT VERSION if old files will become unreadable (or worse, inaccurate)  //
  //                                                                                                  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////
  
  val formatVersion: Int = 1
  
  
  
  // Let's roll our own half-baked CSV because the CIG deadline is in two weeks and json4s is being stubborn and we are wise, experienced developers.
  val separator = ",,,"
  
  def readGames(serializedHistory: Iterable[String]): Vector[HistoricalGame] = {
    serializedHistory.flatMap(readGame).toVector
  }
  
  private def readGame(serializedGame: String): Option[HistoricalGame] = {
    try {
      // Our crude CSV parsing can go wrong in plenty of ways, including unintentional version mismatch.
      // Let's not let bad reads wreck the rest of our data.
      Some(readGameFromColumns(serializedGame.split(separator)))
    }
    catch { case exception: Exception =>
      With.logger.warn("Failed to deserialize game: " + serializedGame)
      With.logger.onException(exception)
      None
    }
  }
  
  private def readGameFromColumns(columns: Array[String]): HistoricalGame = {
    val id            = columns(0).toLong
    val mapName       = columns(1)
    val opponentName  = columns(2)
    val ourRace       = columns(3)
    val enemyRace     = columns(4)
    val won           = columns(5).toBoolean
    val strategies    = columns.drop(6).toSet
    val allRaces = Array(Race.Terran, Race.Protoss, Race.Zerg, Race.Random, Race.None, Race.Unknown)
    HistoricalGame(
      timestamp   = id,
      mapName     = mapName,
      enemyName   = opponentName,
      ourRace     = allRaces.find(_.toString == ourRace).getOrElse(Race.Unknown),
      enemyRace   = allRaces.find(_.toString == enemyRace).getOrElse(Race.Unknown),
      won         = won,
      strategies  = strategies)
  }
  
  def writeGames(games: Iterable[HistoricalGame]): Iterable[String] = {
    games.map(writeGame)
  }
  
  private def writeGame(game: HistoricalGame): String = {
    val columns = List(
      game.timestamp  .toString,
      game.mapName    .toString,
      game.enemyName  .toString,
      game.ourRace    .toString,
      game.enemyRace  .toString,
      game.won        .toString) ++ game.strategies
    columns.mkString(separator)
  }
  
}
