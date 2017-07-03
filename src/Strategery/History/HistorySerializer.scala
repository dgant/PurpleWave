package Strategery.History

object HistorySerializer {
  
  // Let's roll our own half-baked CSV because the CIG deadline is in two weeks and json4s doesn't want to import and we are wise, experienced developers.
  
  val separator = ",,,"
  
  def readGames(serializedHistory: Iterable[String]): Vector[HistoricalGame] = {
    serializedHistory.map(readGame).toVector
  }
  
  private def readGame(serializedGame: String): HistoricalGame = {
    readGameFromColumns(serializedGame.split(separator))
  }
  
  private def readGameFromColumns(columns: Array[String]): HistoricalGame = {
    val id            = columns(0).toInt
    val mapName       = columns(1)
    val opponentName  = columns(2)
    val won           = columns(3).toBoolean
    val strategies    = columns.drop(4).toSet
    HistoricalGame(
      id            = id,
      mapName       = mapName,
      enemyName  = opponentName,
      won           = won,
      strategies    = strategies)
  }
  
  def writeGames(games: Iterable[HistoricalGame]): Iterable[String] = {
    games.map(writeGame)
  }
  
  private def writeGame(game: HistoricalGame): String = {
    val columns = List(game.id.toString, game.mapName.toString, game.enemyName.toString, game.won.toString) ++ game.strategies
    columns.mkString(separator)
  }
  
}
