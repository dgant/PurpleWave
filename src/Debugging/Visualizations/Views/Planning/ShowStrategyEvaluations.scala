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
    var nextX = 100
    nextX = moveX(nextX)
    drawColumn(nextX, "Order",    evaluations.map(_.playbookOrder.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "Samps",    evaluations.map(_.samplesNeeded.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "Games",    evaluations.map(_.games.size.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "vFoe",     evaluations.map(_.gamesVsEnemy.size.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "vRace",    evaluations.map(_.gamesVsRace.size.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "Map",      evaluations.map(_.gamesOnMap.size.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "Win",      evaluations.map(e => formatWinrate(e.winrateTotal)))
    nextX = moveX(nextX)
    drawColumn(nextX, "vFoe",     evaluations.map(e => formatWinrate(e.winrateVsEnemy)))
    nextX = moveX(nextX)
    drawColumn(nextX, "vRace",    evaluations.map(e => formatWinrate(e.winrateVsRace)))
    nextX = moveX(nextX)
    drawColumn(nextX, "Map",      evaluations.map(e => formatWinrate(e.winrateOnMap)))
    nextX = moveX(nextX)
    drawColumn(nextX, "Hope",     evaluations.map(e => formatWinrate(e.interestTotal)))
  }
  
  private def formatWinrate(value: Double): String = {
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
