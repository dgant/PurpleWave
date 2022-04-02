package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowStrategyName extends View {
  
  override def renderScreen() {
    val active = With.strategy.selected.filter(_)
    val inactive = With.strategy.selected.filterNot(_)
    val swapped = With.strategy.deselected
    With.game.drawTextScreen(
      5,
      3 * With.visualization.lineHeightSmall,
      f"${With.strategy.selected.map(_.toString).mkString(" ")} ${if (With.fingerprints.status.nonEmpty) " | " else ""} ${With.fingerprints.status.mkString(" ")}")
  }
}
