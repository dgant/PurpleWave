package Strategery.History

import Debugging.Visualizations.Views.Planning.ShowStrategyEvaluations
import Lifecycle.With

import scala.collection.mutable.ArrayBuffer

object OpponentLogger {

  def onEnd(): Unit = {
    try {
      val columns = ShowStrategyEvaluations.columns
      if (columns.isEmpty) return

      var rows = new ArrayBuffer[String]()

      (0 until columns.map(_.length).max).foreach(rowIndex => {
        var row: String = ""
        columns.foreach(column => {
          if (rowIndex < column.length) row += column(rowIndex)
          row += "\t"
        })
        rows += row
     })

      WriteFile.apply("bwapi-data/write/" + With.enemy.name + ".eval.txt", rows, "write strategy evaluations")
    } catch { case exception: Exception =>
      With.logger.onException(exception)
    }
  }
}
