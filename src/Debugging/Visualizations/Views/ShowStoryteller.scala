package Debugging.Visualizations.Views

import Debugging.Visualizations.Rendering.DrawScreen
import Lifecycle.With
import Utilities.Time.Seconds

object ShowStoryteller extends View {
  val duration = Seconds(10)()

  override def renderScreen(): Unit = {
    val lines = With.storyteller.tales.view.filter(s => s.frame > 0 && With.framesSince(s.frame) < duration).flatMap(_.tale.lines).toVector
    DrawScreen.column(160, 331 - lines.length * (2 + With.visualization.lineHeightSmall), lines)
  }
}
