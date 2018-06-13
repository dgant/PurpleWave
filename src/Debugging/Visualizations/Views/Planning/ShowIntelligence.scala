package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowIntelligence extends View {
  
  override def renderScreen() {
    With.game.drawTextScreen(
      5,
      5 * With.visualization.lineHeightSmall,
      "Matched:\n\n"
      + With.fingerprints.all.filter(_.matches).mkString("\n")
      + "\n\n\nUnmatched:\n\n"
      + With.fingerprints.all.filterNot(_.matches).mkString("\n"))
  }
}
