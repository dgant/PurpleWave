package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowStrategyEvaluations extends View {
  
  override def renderScreen() {
    
    val evaluations = With.strategy.evaluations.values
      .toVector
      .sortBy( - _.interestTotal)
    
    drawColumn(5, "Strategy",     evaluations.map(_.strategy.toString))
    var nextX = 100
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
    drawColumn(nextX, "Win",      evaluations.map(_.winrateTotal.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "vFoe",     evaluations.map(_.winrateVsEnemy.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "vRace",    evaluations.map(_.winrateVsRace.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "Map",      evaluations.map(_.winrateOnMap.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "Expected", evaluations.map(_.interestTotal.toString))
  }
  
  
  
  private def moveX(previousX: Int = 0): Int = {
    previousX + 45
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
