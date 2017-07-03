package Strategery.History

object HistoryLoader {
  
  private val loadFile = "bwapi-data/read/_history.json"
  private val saveFile = "bwapi-data/write/_history.json"
  private val seedFile = "bwapi-data/AI/trainingHistory.json"
  
  def load(): Iterable[HistoricalGame] = {
    val possibleFilenames = Array(loadFile, saveFile)
    val gamesSerialized = "TODO"
    val games = HistorySerializer.readGames(gamesSerialized)
    games
  }
  
  def save(games: Iterable[HistoricalGame]) {
    val gamesSerialized = HistorySerializer.writeGames(games)
  }
}
