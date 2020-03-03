package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowStrategyEvaluations extends View {
  
  override def renderScreen() {

    var nextX = 200
    val columns = this.columns
    drawColumn(5, columns.head.head, columns.head.drop(1))
    columns.drop(1).foreach(column => {
      nextX += 40
      drawColumn(nextX, column.head, column.drop(1))
    })
  }

  def columns: Vector[Vector[String]] = {
    val evaluations = With.strategy.evaluations.values
      .toVector
      .sortBy(_.playbookOrder)
      .sortBy( - _.probabilityWin)

    Vector(
      Vector("Strategy") ++ evaluations.map(_.strategy.toString),
      Vector("Order")    ++ evaluations.map(e => if (e.playbookOrder > 100) "" else e.playbookOrder.toString),
      Vector("#Games")   ++ evaluations.map(e => formatGames(e.gamesUs.size)),
      Vector("#Wtd")     ++ evaluations.map(e => formatGames(e.gamesUs.map(_.weight).sum)),
      Vector("#WtdWins") ++ evaluations.map(e => formatGames(e.gamesUs.map(_.winsWeighted).sum)),
      Vector("WinPct")   ++ evaluations.map(e => formatPercentage(e.winrateVsEnemy)),
      Vector("WinEst")   ++ evaluations.map(e => formatPercentage(e.probabilityWin)))
  }
  
  private def formatGames(games: Double): String = {
    "%1.1f".format(games)
  }
  
  private def formatPercentage(value: Double): String = {
    (value * 100.0).toInt + """%%"""
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
