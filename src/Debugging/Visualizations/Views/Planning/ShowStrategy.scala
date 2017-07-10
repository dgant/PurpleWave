package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowStrategy extends View {
  
  override def renderScreen() {
    With.game.drawTextScreen(
      5,
      3 * With.visualization.lineHeightSmall,
      With.strategy.selected.map(_.toString).mkString(", "))
  }
}
