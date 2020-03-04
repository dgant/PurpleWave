package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowStrategyInterest extends View {

  def evaluations: Vector[(String, String)] = With.strategy.winProbabilityByBranchLegal
    .toVector
    .sortBy( - _._2)
    .map(pair => (formatPercentage(pair._2), pair._1.toSeq.map(_.toString).sorted.mkString(" + ")))
  
  override def renderScreen() {
    val x0 = 5
    val x1 = 45
    val y0 = 5 * With.visualization.lineHeightSmall
  
    With.game.drawTextScreen(x0, y0, "Interest")
    With.game.drawTextScreen(x1, y0, "Strategy")
    
    evaluations
      .take(380 / With.visualization.lineHeightSmall)
      .indices
      .foreach(i => {
        val y = y0 + (1 + i) * With.visualization.lineHeightSmall
        val e = evaluations(i)
        With.game.drawTextScreen(x0, y, e._1)
        With.game.drawTextScreen(x1, y, e._2)
      })
  }
  
  private def formatPercentage(value: Double): String = {
    (value * 100.0).toInt + """%%"""
  }
}
