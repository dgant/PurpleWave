package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Views.View
import Information.StrategyDetection.ZergStrategies._
import Lifecycle.With

object ShowIntelligence extends View {
  
  val fingerprints = Vector(
    new Fingerprint4Pool,
    new Fingerprint9Pool,
    new FingerprintOverpool,
    new Fingerprint10Hatch9Pool,
    new Fingerprint12Hatch,
    new Fingerprint12Pool
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
