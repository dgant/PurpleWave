package Debugging.Visualizations.Views.Economy

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowScheduler extends View {
  
  override def renderScreen() {
    With.game.drawTextScreen(5, 5 * With.visualization.lineHeightSmall, "Scheduler queue")
    DrawScreen.table(5, 7 * With.visualization.lineHeightSmall,
      With.scheduler.queue
        .take(15)
        .map(buildable => Vector(buildable.toString)))
  }
  
  private def firstWord(text: String): String = text.split("\\s+")(0)
  
  private def reframe(frameAbsolute:Int):String = {
    val reframed = (frameAbsolute - With.frame)/24
    if (reframed <= 0) "Started" else reframed.toString
  }
}
