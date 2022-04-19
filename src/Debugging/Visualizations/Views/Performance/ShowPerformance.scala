package Debugging.Visualizations.Views.Performance

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import Mathematics.Maff
import Performance.Tasks.TimedTask

object ShowPerformance extends DebugView {
  
  override def renderScreen(): Unit = {
    DrawScreen.table(5, With.visualization.lineHeightSmall * 6, statusTable(sortTasks(With.performance.tasks)).take(20))
  }

  def sortTasks(tasks: Seq[TimedTask]): Seq[TimedTask] = tasks
    .sortBy(_.getClass.getSimpleName)
    .sortBy(-_.runMsTotal)
    .sortBy(-_.runsCrossingTarget)
    .sortBy(-_.runsCrossingLimit)

  def statusTable(tasks: Seq[TimedTask]): Seq[Seq[String]] = {
    val title = Vector("Target:", f"${With.configuration.frameTargetMs}ms", "Cutoff:", f"${With.configuration.frameLimitMs}ms")
    val headers = Vector("Task", "Runs", "Seconds", "%Time", "Budgt (Recnt)", "Avg ms", "Max (Rcnt)", "Max (All time)", "AcrossTarget", "AcrossLimit")
    val body = tasks
      .map(task => Vector(
        task.toString,
        task.runsTotal.toString,
        (task.runMsTotal / 1000).toString,
        f"${(100.0 * task.runMsTotal / (With.performance.systemMillis - With.performance.gameStartMs)).toInt}%%",
        Maff.meanL(task.budgetMsPast).toInt.toString,
        task.runMsRecentMean.toString,
        task.runMsRecentMax().toString,
        task.runMsMax.toString,
        task.runsCrossingTarget.toString,
        task.runsCrossingLimit.toString
      ))
    Vector(title) ++ Vector(headers) ++ body
  }
}
