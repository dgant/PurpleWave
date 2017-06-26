package Debugging.Visualizations.Views

import Lifecycle.With

object ScreenClock {
  
  def render() {
    val totalSeconds = With.frame * 42 / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val clock = minutes + ":" + "%02d".format(seconds)
    With.game.drawTextScreen(380, 5, clock)
    With.game.drawTextScreen(410, 5, With.frame.toString)
  }
}
