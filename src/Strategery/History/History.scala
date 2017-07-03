package Strategery.History

import Lifecycle.With
import Utilities.CountMap

class History {
  
  private lazy val games: Iterable[HistoricalGame] = HistoryLoader.load()
  
  def currentMap      : String = With.game.mapFileName
  def currentOpponent : String = With.enemies.head.name
  
  def onEnd(weWon: Boolean) {
    val nextId = games.map(_.id).max + 1
    val thisGame = HistoricalGame(
      id            = nextId,
      mapName       = currentMap,
      opponentName  = currentOpponent,
      won           = weWon,
      strategies    = With.strategy.selected.map(_.toString))
    HistoryLoader.save(games.toVector :+ thisGame)
  }
  
  def getMapHistory(mapName: String): ContextualHistory = {
    getHistory(games.filter(_.mapName == mapName))
  }
  
  def getOpponentHistory(opponentName: String): ContextualHistory = {
    getHistory(games.filter(_.opponentName == opponentName))
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
