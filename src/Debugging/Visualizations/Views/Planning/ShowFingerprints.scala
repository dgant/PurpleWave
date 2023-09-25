package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With

object ShowFingerprints extends DebugView {

  override def renderScreen(): Unit = {
    DrawScreen.text(
      5,
      5 * With.visualization.lineHeightSmall,
      "Matched:\n\n"
      + With.fingerprints.status.mkString("\n")
    )
  }
}
