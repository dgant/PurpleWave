package Debugging.Visualizations.Views.Performance

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowPerformanceDetails extends View {
  
  override def renderScreen() {
    val title = Vector("Cutoff: ", With.configuration.targetFrameDurationMilliseconds + "ms")
    val headers = Vector("Task", "Last run", " Run %", "Seconds", "Avg ms", "Max (Recent)", "Max (All time)", "Extended", "Disqualifying")
    val body = With.tasks.tasks
      .sortBy(_.getClass.getSimpleName)
      .map(task => Vector(
        task.getClass.getSimpleName.replace("Task", ""),
        "X" * Math.min(10, Math.max(0, task.framesSinceRunning - 1)),
        " " + (100 * (1.0 + task.totalRuns) / (1.0 + task.totalSkips + task.totalRuns)).toInt.toString + "%%",
        (task.runMillisecondsTotal / 1000).toString,
        task.runMillisecondsMean.toString,
        task.runMillisecondsMaxRecent().toString,
        task.runMillisecondsMaxAllTime.toString,
        task.totalViolatedThreshold.toString,
        task.totalViolatedRules.toString
      ))
    DrawScreen.table(5, With.visualization.lineHeightSmall * 6, With.tasks.statusTable)
  }
}
