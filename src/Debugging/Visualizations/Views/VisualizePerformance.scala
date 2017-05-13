package Debugging.Visualizations.Views

import Debugging.Visualizations.Rendering.DrawScreen
import Lifecycle.With

object VisualizePerformance {
  def render() {
    DrawScreen.header(50,  With.game.getLatencyFrames             + " latency frames")
    DrawScreen.header(125, With.latency.turnSize                  + " frames/turn")
    DrawScreen.header(200, With.performance.meanFrameMilliseconds + "ms avg")
    DrawScreen.header(275, With.performance.maxFrameMilliseconds  + "ms max")
    DrawScreen.header(350, With.performance.cacheLength(1)        + " cache duration")
    With.game.drawTextScreen(120, 400, "+55ms:    " + With.performance.framesOver55)
    With.game.drawTextScreen(180, 400, "+1000ms:  " + With.performance.framesOver1000)
    With.game.drawTextScreen(240, 400, "+10000ms: " + With.performance.framesOver10000)
    if (With.performance.disqualified) {
      With.game.setTextSize(bwapi.Text.Size.Enum.Large)
      With.game.drawTextScreen(300, 360, "DISQUALIFIED")
      With.game.setTextSize(bwapi.Text.Size.Enum.Small)
    }
    
    if (With.configuration.visualizePerformanceDetails) {
      renderDetails()
    }
  }
  
  def renderDetails() {
    val title = Vector("Cutoff: ", With.configuration.peformanceFrameMilliseconds + "ms")
    val headers = Vector("System", "Last run", "Total runs", "Total skips", "Avg ms", "Max ms")
    val body = With.tasks.tasks
      .sortBy(_.getClass.getSimpleName + "  ")
      .map(system => Vector(
        system.getClass.getSimpleName.replace("Task", ""),
        "X" * Math.min(10, Math.max(0, system.framesSinceRunning - 1)),
        system.totalRuns.toString,
        system.totalSkips.toString,
        system.runMillisecondsMean.toString,
        system.runMillisecondsMax.toString
      ))
    DrawScreen.table(300, 27, Vector(title) ++ Vector(headers) ++ body)
  }
}
