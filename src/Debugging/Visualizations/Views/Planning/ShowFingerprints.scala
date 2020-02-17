package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowFingerprints extends View {

  override def renderScreen() {
    With.game.drawTextScreen(
      5,
      5 * With.visualization.lineHeightSmall,
      "Matched:\n\n"
      + With.fingerprints.status.mkString("\n")
    )
  }
}
