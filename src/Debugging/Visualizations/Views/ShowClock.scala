package Debugging.Visualizations.Views

import Lifecycle.With

object ShowClock extends View {
  
  override def renderScreen () {
    val totalSeconds = With.frame * 42 / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val clock = minutes + ":" + "%02d".format(seconds)
    With.game.drawTextScreen(375, With.visualization.lineHeightSmall, clock)
    With.game.drawTextScreen(405, With.visualization.lineHeightSmall, With.frame.toString)
  }
}
