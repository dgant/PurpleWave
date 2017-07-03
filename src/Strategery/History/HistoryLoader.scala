package Strategery.History

import java.io._

import Lifecycle.With

object HistoryLoader {
  
  private val loadFile = "bwapi-data/read/_history.json"
  private val saveFile = "bwapi-data/write/_history.json"
  private val seedFile = "bwapi-data/AI/trainingHistory.json"
  private val possibleFilenames = Array(loadFile, saveFile, seedFile)
  
  def load(): Iterable[HistoricalGame] = {
    val gamesSerialized = loadBestFile(possibleFilenames)
    val games = HistorySerializer.readGames(gamesSerialized)
    games
  }
  
  def save(games: Iterable[HistoricalGame]) {
    val gamesSerialized = HistorySerializer.writeGames(games)
  }
  
  private def loadBestFile(possibleFilenames: Iterable[String]): String = {
    possibleFilenames
      .view
      .map(loadFile(_))
      .find(_.isDefined)
      .map(_.get)
      .getOrElse("")
  }
  
  private def loadFile(filename: String): Option[String] = {
    var output: Option[String] = None
    val file    = new File(filename)
    val stream  = new FileInputStream(file)
    val reader  = new BufferedReader(new InputStreamReader(stream))
    try {
      val lines = new StringBuilder
      var continueReading = true
      while (continueReading) {
        val nextLine = reader.readLine()
        continueReading = nextLine == null
        if (continueReading) {
          lines.append(nextLine)
        }
      }
      output = Some(lines.toString)
    }
    catch { case exception: Exception =>
      With.logger.warn("Failed to load game history from " + filename)
      With.logger.onException(exception)
    }
    reader.close()
    output
  }
}
