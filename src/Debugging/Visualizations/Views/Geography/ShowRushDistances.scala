package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.PurpleMath

object ShowRushDistances extends View {
  
  override def renderScreen() {
    val x = 5
    val y = 2 * With.visualization.lineHeightSmall
    val distances = With.geography.rushDistances
    DrawScreen.table(
      x,
      y,
      Vector(
        Vector(
          "Rush distances: ",
          distances.min.toString,
          PurpleMath.mean(distances).toString,
          distances.max.toString
        )))
  }
  
}
