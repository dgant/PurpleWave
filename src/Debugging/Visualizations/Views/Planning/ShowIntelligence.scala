package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Views.View
import Information.StrategyDetection.ZergStrategies._
import Lifecycle.With

object ShowIntelligence extends View {
  
  val fingerprints = Vector(
    Fingerprint4Pool,
    Fingerprint9Pool,
    FingerprintOverpool,
    Fingerprint12Hatch,
    Fingerprint12Pool
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
