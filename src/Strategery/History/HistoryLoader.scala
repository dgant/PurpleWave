package Strategery.History

import java.io._
import java.nio.file.Paths

import Lifecycle.With

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object HistoryLoader {
  
  private val filenameHistoryPrefix = "_v" + HistorySerializer.formatVersion + "_history_"
  private val filenameEnemyToken = "{opponent}"
  private val filenameTemplate = filenameHistoryPrefix + filenameEnemyToken + ".csv"
  private def loadFilesDirectory = With.bwapiData.read
  private def saveFilesDirectory = With.bwapiData.write
  private val seedFilesDirectories = Array("", "PretrainP", "PretrainT", "PretrainZ")
    .map(relative => With.bwapiData.ai + relative)
  
  // The order matters (see below) thus meriting the explicit naming
  private val directoriesInDecendingOrderOfRecency = Array(loadFilesDirectory, saveFilesDirectory) ++ seedFilesDirectories
  
  def load(): Seq[HistoricalGame] = {
    val gamesSerialized = loadAllGames(directoriesInDecendingOrderOfRecency)
    val games = HistorySerializer.readGames(gamesSerialized)
    
    // Loading history from multiple potentially-redundant sources means some games may appear in the history multiple times.
    // Let's distinct-ify them by timestamp to prevent that.
    //
    // As an added check, we are implicitly trusting the more "recent" (read -> write -> seeded training) game associated with a timestamp
    //
    val gamesByTimestamp = new mutable.HashMap[Long, HistoricalGame]
    games.foreach(game => gamesByTimestamp(game.timestamp) = gamesByTimestamp.getOrElse(game.timestamp, game))
    val output = gamesByTimestamp.values.toSeq.sortBy(_.timestamp).reverse
    output.zipWithIndex.foreach(pair => pair._1.order = pair._2)
    output
  }
  
  def save(games: Iterable[HistoricalGame]) {
    val gamesVsOpponent       = games.filter(_.enemyName == With.history.currentEnemyName)
    val gamesToSave           = gamesVsOpponent.toSeq.sortBy(- _.timestamp).take(With.configuration.maximumGamesHistoryPerOpponent)
    val gamesToSaveSerialized = HistorySerializer.writeGames(gamesToSave)
    saveGames(saveName, gamesToSaveSerialized)
  }
  
  private def saveName: String = {
    saveFilesDirectory +
    filenameTemplate.replace(filenameEnemyToken, With.history.currentEnemyName)
  }
  
  private def loadAllGames(directories: Iterable[String]): Iterable[String] = {
    directories.flatMap(loadGamesFromDirectory)
  }
  
  private def loadGamesFromDirectory(directory: String): Iterable[String] = {
    // I don't think this can actually throw, but let's wear some tinfoil.
    try {
      var files: Array[File] = new File(directory).listFiles
      if (files == null) {
        files = Array.empty
      }
      val historyFiles = files.filter(_.getName.contains(filenameHistoryPrefix))
      return historyFiles.flatMap(loadGamesFromFile)
    }
    catch { case exception: Exception =>
      With.logger.warn("Failed to read games directory " + directory)
      With.logger.onException(exception)
    }
    Iterable.empty
  }
  
  private def loadGamesFromFile(file: File): Iterable[String] = {
    
    var reader: BufferedReader = null
    var output: Iterable[String] = Iterable.empty
    var filename = "[Unknown file]"
    
    try {
      if (file.exists) {
            filename  = file.getName
        var proceed   = true
        val lines     = new ArrayBuffer[String]
        val stream    = new FileInputStream(file)
            reader    = new BufferedReader(new InputStreamReader(stream))
        while (proceed) {
          val nextLine = reader.readLine()
          proceed = nextLine != null
          if (proceed) {
            lines += nextLine
          }
        }
  
        output = lines
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
