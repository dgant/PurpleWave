package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Views.View
import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With

object ShowFingerprints extends View {

  private def format(finger: Fingerprint): String = {
    finger.toString.replaceAll("Fingerprint", "")
  }

  override def renderScreen() {
    With.game.drawTextScreen(
      5,
      5 * With.visualization.lineHeightSmall,
      "Matched:\n\n"
      + With.fingerprints.all.filter(_.matches).map(format).mkString("\n")
      + "\n\n\nUnmatched:\n\n"
      + With.fingerprints.all.filterNot(_.matches).map(format).mkString("\n"))
  }
}
