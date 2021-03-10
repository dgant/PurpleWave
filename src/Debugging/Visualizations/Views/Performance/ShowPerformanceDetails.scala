package Debugging.Visualizations.Views.Performance

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.PurpleMath
import Performance.Tasks.TimedTask

object ShowPerformanceDetails extends View {
  
  override def renderScreen() {
    DrawScreen.table(5, With.visualization.lineHeightSmall * 6, statusTable(sortTasks(With.performance.tasks)).take(20))
  }

  def sortTasks(tasks: Seq[TimedTask]): Seq[TimedTask] = tasks
    .sortBy(_.getClass.getSimpleName)
    .sortBy(-_.runMsTotal)
    .sortBy(-_.runsCrossingTarget)
    .sortBy(-_.runsCrossingLimit)

  def statusTable(tasks: Seq[TimedTask]): Seq[Seq[String]] = {
    val title = Vector("Target:", With.configuration.frameTargetMs + "ms", "", "Cutoff:", With.configuration.frameLimitMs + "ms")
    val headers = Vector("Task", "Last run", "Seconds", "Budget (Recent)", "Avg ms", "Max (Recent)", "Max (All time)", "AcrossTarget", "AcrossLimit")
    val body = tasks
      .map(task => Vector(
        task.toString,
        "X" * Math.min(10, Math.max(0, task.framesSinceRunning - 1)),
        (task.runMsTotal / 1000).toString,
        PurpleMath.meanL(task.budgetMsPast).toInt.toString,
        task.runMsRecentMean.toString,
        task.runMsRecentMax().toString,
        task.runMsMax.toString,
        task.runsCrossingTarget.toString,
        task.runsCrossingLimit.toString
      ))
    Vector(title) ++ Vector(headers) ++ body
  }
}
