package Debugging.Visualizations.Views.Performance

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowPerformanceDetails extends View {
  
  override def renderScreen() {
    val title = Vector("Cutoff: ", With.configuration.initialTaskLengthMilliseconds + "ms")
    val headers = Vector("System", "Last run", "Total runs", "Total skips", "Avg ms", "Max (Recent)", "Max (All time)", "Threshold :(", "Rules :(")
    val body = With.tasks.tasks
      .sortBy(_.getClass.getSimpleName)
      .map(task => Vector(
        task.getClass.getSimpleName.replace("Task", ""),
        "X" * Math.min(10, Math.max(0, task.framesSinceRunning - 1)),
        task.totalRuns.toString,
        task.totalSkips.toString,
        task.runMillisecondsMean.toString,
        task.runMillisecondsMaxRecent.toString,
        task.runMillisecondsMaxAllTime.toString,
        task.totalViolatedThreshold.toString,
        task.totalViolatedRules.toString
      ))
    DrawScreen.table(5, With.visualization.lineHeightSmall * 8, Vector(title) ++ Vector(headers) ++ body)
  }
}
