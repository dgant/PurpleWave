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
    renderDetails()
  }
  
  def renderDetails() {
    val headers = Vector("System", "Last run", "Total runs", "Avg ms", "Max ms")
    val body = With.systems.systems
      .sortBy(_.getClass.getSimpleName)
      .map(system => Vector(
        system.getClass.getSimpleName.replace("System", ""),
        system.framesSinceRunning.toString,
        system.totalRuns.toString,
        system.runMillisecondsMean.toString,
        system.runMillisecondsMax.toString
      ))
    DrawScreen.table(300, 27, Vector(headers) ++ body)
  }
}
