package Strategery.History

import Lifecycle.{Manners, With}
import Utilities.CountMap

class History {
  
  private lazy val games: Iterable[HistoricalGame] = HistoryLoader.load()
  
  lazy val currentMapName   : String = With.game.mapFileName
  lazy val currentEnemyName : String = With.enemies.head.name
  
  def onStart() {
    Manners.chat("Good luck on " + currentMapName + ", " + currentEnemyName + "!")
    
    val mapWins   = games.count(g => g.mapName == currentMapName &&   g.won)
    val mapLosses = games.count(g => g.mapName == currentMapName && ! g.won)
    val vsWins   = games.count(g => g.enemyName == currentEnemyName && g .won)
    val vsLosses = games.count(g => g.enemyName == currentEnemyName && ! g.won)
    Manners.chat("Record on " + currentMapName + ": " + mapWins + " - " + mapLosses)
    Manners.chat("Record vs. " + currentEnemyName + ": " + vsWins + " - " + vsLosses)
  }
  
  def onEnd(weWon: Boolean) {
    val nextId = if (games.isEmpty) 0 else games.map(_.id).max + 1
    val thisGame = HistoricalGame(
      id            = nextId,
      mapName       = currentMapName,
      enemyName     = currentEnemyName,
      won           = weWon,
      strategies    = With.strategy.selected.map(_.toString))
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
