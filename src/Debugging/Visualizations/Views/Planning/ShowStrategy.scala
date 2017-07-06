package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowStrategy extends View {
  
  override def renderScreen() {
    With.game.drawTextScreen(
      375,
      2 * With.visualization.lineHeightSmall,
      With.strategy.selected.map(_.toString).mkString(", "))
  }
}
