package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowStrategiesInterest extends View {
  
  override def renderScreen() {
    
    val evaluations = With.strategy.permutationInterest
      .toVector
      .sortBy( - _._2)
      .take(380 / With.visualization.lineHeightSmall)
    
    val x0 = 5
    val x1 = 45
    val y0 = 5 * With.visualization.lineHeightSmall
  
    With.game.drawTextScreen(x0, y0, "Interest")
    With.game.drawTextScreen(x1, y0, "Strategy")
    
    evaluations
      .indices
      .foreach(i => {
        val y = y0 + (1 + i) * With.visualization.lineHeightSmall
        val e = evaluations(i)
        With.game.drawTextScreen(x0, y, formatPercentage(e._2))
        With.game.drawTextScreen(x1, y, e._1.toSeq.map(_.toString).sorted.mkString(" + "))
      })
  }
  
  private def formatPercentage(value: Double): String = {
    (value * 100.0).toInt + """%%"""
  }
}
