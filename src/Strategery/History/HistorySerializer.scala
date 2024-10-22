package Strategery.History

import Lifecycle.With
import Utilities.CountMap
import bwapi.Race

object HistorySerializer {
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////
  //                                                                                                  //
  // Hey, are you updating this file?                                                                 //
  // Consider updating the FORMAT VERSION if old files will become unreadable (or worse, inaccurate)  //
  //                                                                                                  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////
  
  val formatVersion: Int = 4
  
  // "Let's roll our own half-baked CSV because the CIG deadline is in two weeks and json4s is being stubborn and we are wise, experienced developers."
  // The half-backed CSV has lasted 7.5 years now.
  val separator = ","
  
  def readGames(serializedHistory: Iterable[String]): Vector[HistoricalGame] = {
    serializedHistory.flatMap(readGame).toVector
  }
  
  private def readGame(serializedGame: String): Option[HistoricalGame] = {
    try {
      Some(readGameFromColumns(serializedGame.replaceAll(",,,", separator).split(separator).map(_.trim)))
    } catch { case exception: Exception =>
      With.logger.warn(f"Failed to deserialize game: $serializedGame")
      With.logger.onException(exception)
      None
    }
  }
  
  private def readGameFromColumns(columns: Array[String]): HistoricalGame = {
    val id              = columns(0).toLong
    val startLocations  = columns(1).toInt
    val mapName         = columns(2)
    val opponentName    = columns(3)
    val ourRace         = columns(4)
    val enemyRace       = columns(5)
    val won             = columns(6).toBoolean
    val tags            = columns.drop(7).map(_.replaceAll("Fingerprint", "Finger").replaceAll("Finger", "&")).distinct
    val allRaces        = Array(Race.Terran, Race.Protoss, Race.Zerg, Race.Random, Race.None, Race.Unknown)
    HistoricalGame(
      timestamp       = id,
      startLocations  = startLocations,
      mapName         = mapName,
      enemyName       = opponentName,
      ourRace         = allRaces.find(_.toString == ourRace)  .getOrElse(Race.Unknown),
      enemyRace       = allRaces.find(_.toString == enemyRace).getOrElse(Race.Unknown),
      won             = won,
      tags            = tags)
  }
  
  def writeGames(games: Iterable[HistoricalGame]): Vector[String] = {
    val columnLengths = new CountMap[Int]()
    val gameStrings: Vector[Vector[String]] = games.toVector.sortBy(-_.timestamp).map(writeGame)
    gameStrings.foreach (_.zipWithIndex.foreach { case (cell, index) => columnLengths(index) = Math.max(columnLengths(index), cell.length) })
    gameStrings.map     (_.zipWithIndex.map     { case (cell, index) => (cell + separator).padTo(2 + columnLengths(index), ' ') }.mkString)

  }
  
  private def writeGame(game: HistoricalGame): Vector[String] = {
    Vector(
      game.timestamp      .toString,
      game.startLocations .toString,
      game.mapName,
      game.enemyName,
      game.ourRace        .toString,
      game.enemyRace      .toString,
      game.won            .toString) ++ game.tags
  }
  
}
