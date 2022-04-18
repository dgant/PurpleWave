package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Views.DebugView
import Lifecycle.With

object ShowFingerprints extends DebugView {

  override def renderScreen() {
    With.game.drawTextScreen(
      5,
      5 * With.visualization.lineHeightSmall,
      "Matched:\n\n"
      + With.fingerprints.status.mkString("\n")
    )
  }
}
