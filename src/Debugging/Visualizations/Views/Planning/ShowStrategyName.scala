package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowStrategyName extends View {
  
  override def renderScreen() {
    With.game.drawTextScreen(
      5,
      3 * With.visualization.lineHeightSmall,
      f"${With.strategy.selectedCurrently.map(_.toString).mkString(" ")} ${if (With.fingerprints.status.nonEmpty) " | " else ""} ${With.fingerprints.status.mkString(" ")}")
  }
}
