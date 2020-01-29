package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.Points.Pixel

object ShowStatus extends View {
  
  override def renderScreen() {
    DrawScreen.text(Pixel(230, 2 * With.visualization.lineHeightSmall), With.blackboard.status.get.mkString(", "))
  }
}
