package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Planning.UnitMatchers.UnitMatchWorkers

import scala.collection.mutable.ArrayBuffer

object ShowResources extends View {
  override def renderScreen() {
    
    val rawText = With.bank.requests
      .take(25)
      .map(request =>
        Iterable(
          "",
          if (request.isSpent)
            "Spent"
          else if (request.satisfied)
            "Available"
          else if (request.onSchedule)
            "On schedule"
          else
            "",
          if (request.expectedFrames > 0 && request.expectedFrames < 24 * 60 * 5) (request.expectedFrames/24).toString + " seconds" else "",
            (if (request.minerals > 0)  request.minerals + "m " else "") +
            (if (request.gas      > 0)  request.gas      + "g " else "") +
            (if (request.supply   > 0)  request.supply   + "s " else ""),
          request.owner.toString
        ))
          
    var lineLast = Iterable("not happenin")
    val output = new ArrayBuffer[Iterable[String]]()
    output += Vector("", "", "", "Workers:", With.units.countOurs(UnitMatchWorkers).toString)
    rawText.foreach(lineNext => {
      var lineFinal = lineNext
      if (lineFinal.drop(1) == lineLast.drop(1)) {
        output.trimEnd(1)
        lineFinal = lineLast
        if (lineFinal.head == "") {
          lineFinal = Iterable("x2") ++ lineFinal.drop(1)
        }
        else {
          val x = lineFinal.head
          lineFinal = lineFinal.drop(1)
          lineFinal = Iterable("x" + (x.drop(1).toInt + 1).toString) ++ lineFinal
        }
      }
      output += lineFinal
      lineLast = lineFinal
    })
    
    DrawScreen.table(
      240,
      4 * With.visualization.lineHeightSmall,
      output)
  }
}
