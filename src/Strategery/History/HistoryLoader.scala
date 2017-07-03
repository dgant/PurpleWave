package Strategery.History

import java.io._

import Lifecycle.With

import scala.collection.mutable.ArrayBuffer

object HistoryLoader {
  
  private val loadFile = "bwapi-data/read/_history.csv"
  private val saveFile = "bwapi-data/write/_history.csv"
  private val seedFile = "bwapi-data/AI/trainingHistory.csv"
  private val possibleFilenames = Array(loadFile, saveFile, seedFile)
  
  def load(): Iterable[HistoricalGame] = {
    val gamesSerialized = loadBestGames(possibleFilenames)
    val games = HistorySerializer.readGames(gamesSerialized)
    games
  }
  
  def save(games: Iterable[HistoricalGame]) {
    val gamesSerialized = HistorySerializer.writeGames(games)
    saveGames(saveFile, gamesSerialized)
  }
  
  
  private def loadBestGames(possibleFilenames: Iterable[String]): Iterable[String] = {
    possibleFilenames
      .view
      .map(loadGames)
      .find(_.isDefined)
      .map(_.get)
      .getOrElse(List[String]())
  }
  
  private def loadGames(filename: String): Option[Iterable[String]] = {
    
    var reader: BufferedReader = null
    var output: Option[Iterable[String]] = None
    
    try {
      val file    = new File(filename)
      if (file.exists) {
        var proceed = true
        val lines   = new ArrayBuffer[String]
        val stream  = new FileInputStream(file)
            reader  = new BufferedReader(new InputStreamReader(stream))
        
        while (proceed) {
          val nextLine = reader.readLine()
          proceed = nextLine != null
          if (proceed) {
            lines += nextLine
          }
        }
  
        output = Some(lines)
      }
    }
    catch { case exception: Exception =>
      With.logger.warn("Failed to load game history from " + filename)
      With.logger.onException(exception)
    }
    if (reader != null) {
      reader.close()
    }
    output
  }
  
  private def saveGames(filename: String, contents: Iterable[String]) {
    
    var bufferedWriter: BufferedWriter = null
    
    try {
      val file            = new File(filename)
      val fileWriter      = new FileWriter(file)
          bufferedWriter  = new BufferedWriter(fileWriter)
      
      contents.map(_ + "\n").foreach(bufferedWriter.write)
    }
    catch { case exception: Exception =>
      With.logger.warn("Failed to save game history to " + filename)
      With.logger.onException(exception)
    }
    if (bufferedWriter != null) {
      bufferedWriter.close()
    }
  }
}
