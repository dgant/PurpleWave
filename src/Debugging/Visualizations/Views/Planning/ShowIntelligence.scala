package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowIntelligence extends View {
  
  def fingerprints = Vector(
    With.intelligence.fingerprints.fingerprint4Pool,
    With.intelligence.fingerprints.fingerprint9Pool,
    With.intelligence.fingerprints.fingerprintOverpool,
    With.intelligence.fingerprints.fingerprint10Hatch9Pool,
    With.intelligence.fingerprints.fingerprint12Pool,
    With.intelligence.fingerprints.fingerprint12Hatch
  )
  
  override def renderScreen() {
    With.game.drawTextScreen(
      5,
      7 * With.visualization.lineHeightSmall,
      "Matched:\n\n"
      + fingerprints.filter(_.matches).mkString("\n")
      + "\n\n\nUnmatched:\n\n"
      + fingerprints.filterNot(_.matches).mkString("\n"))
  }
}
