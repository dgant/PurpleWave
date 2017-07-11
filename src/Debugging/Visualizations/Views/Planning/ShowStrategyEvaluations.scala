package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowStrategyEvaluations extends View {
  
  override def renderScreen() {
    
    val evaluations = With.strategy.evaluations.values
      .toVector
      .sortBy( - _.interestTotal)
    
    drawColumn(5, "Strategy", evaluations.map(_.strategy.toString))
    var nextX = 50
    nextX = moveX(nextX)
    drawColumn(nextX, "Samples",    evaluations.map(_.samplesNeeded.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "Games",      evaluations.map(_.games.size.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "Vs. Foe",    evaluations.map(_.gamesVsEnemy.size.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "Vs. Race",   evaluations.map(_.gamesVsRace.size.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "On map",     evaluations.map(_.gamesOnMap.size.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "Winrate",    evaluations.map(_.winrateTotal.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "Vs. Foe",    evaluations.map(_.winrateVsEnemy.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "Vs. Race",   evaluations.map(_.winrateVsRace.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "On map",     evaluations.map(_.winrateOnMap.toString))
    nextX = moveX(nextX)
    drawColumn(nextX, "Expected",   evaluations.map(_.interestTotal.toString))
  }
  
  private def moveX(previousX: Int = 0): Int = {
    previousX + 30
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
