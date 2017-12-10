package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowStrategyEvaluations extends View {
  
  override def renderScreen() {
    
    val evaluations = With.strategy.evaluations.values
      .toVector
      .sortBy(_.playbookOrder)
      .sortBy( - _.interestTotal)
    
    drawColumn(5, "Strategy",     evaluations.map(_.strategy.toString))
    var nextX = 85
    nextX = moveX(nextX)
    drawColumn(nextX, "Order",    evaluations.map(e => if (e.playbookOrder > 100) "" else e.playbookOrder.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "Samps",    evaluations.map(_.patienceGames.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "Game",     evaluations.map(_.games.size.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "GameW",    evaluations.map(e => formatGames(e.games.map(_.weight).sum)))
    nextX = moveX(nextX)
    drawColumn(nextX, "vFoeW",    evaluations.map(e => formatGames(e.gamesVsEnemy.map(_.weight).sum)))
    nextX = moveX(nextX)
    drawColumn(nextX, "vRaceW",   evaluations.map(e => formatGames(e.gamesVsRace.map(_.weight).sum)))
    nextX = moveX(nextX)
    drawColumn(nextX, "onMapW",   evaluations.map(e => formatGames(e.gamesOnMap.map(_.weight).sum)))
    nextX = moveX(nextX)
    drawColumn(nextX, "Win",      evaluations.map(e => formatPercentage(e.winrateTotal)))
    nextX = moveX(nextX)
    drawColumn(nextX, "vFoe",     evaluations.map(e => formatPercentage(e.winrateVsEnemy)))
    nextX = moveX(nextX)
    drawColumn(nextX, "vRace",    evaluations.map(e => formatPercentage(e.winrateVsRace)))
    nextX = moveX(nextX)
    drawColumn(nextX, "Map",      evaluations.map(e => formatPercentage(e.winrateOnMap)))
    nextX = moveX(nextX)
    drawColumn(nextX, "Interest", evaluations.map(e => formatPercentage(e.interestTotal)))
  }
  
  private def formatGames(games: Double): String = {
    "%1.1f".format(games)
  }
  
  private def formatPercentage(value: Double): String = {
    (value * 100.0).toInt + """%%"""
  }
  
  private def moveX(previousX: Int = 0): Int = {
    previousX + 35
  }
  
  private def drawColumn(x: Int, name: String, rows: Iterable[String]) {
    val y = 5 * With.visualization.lineHeightSmall
    With.game.drawTextScreen(x, y, name)
    DrawScreen.column(
      x,
      y + 2 * With.visualization.lineHeightSmall,
      rows)
  }
}
