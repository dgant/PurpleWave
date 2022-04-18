package Debugging.Visualizations.Views.Economy

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With

object ShowUnitCounts extends DebugView {

  override def renderScreen(): Unit = {
    val unitCounts = With.units.ours
      .groupBy(_.unitClass)
      .toVector
      .sortBy(-_._2.size)

    if (unitCounts.nonEmpty) {
      val text = Vector(Vector("Our units", "")) ++ unitCounts.map(row => Vector(row._2.size.toString, row._1.toString))
      DrawScreen.table(480, 5 * With.visualization.lineHeightSmall, text)
    }
  }
}
