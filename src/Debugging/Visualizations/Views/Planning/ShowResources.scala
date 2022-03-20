package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Planning.UnitMatchers.MatchWorker

import scala.collection.mutable.ArrayBuffer

object ShowResources extends View {

  val dropColumns = 4

  override def renderScreen() {

    var minerals = With.self.minerals
    var gas = With.self.gas
    var supply = With.self.supplyTotal - With.self.supplyUsed
    val rawText = With.bank.requests
      .toVector
      .sortBy(_.expectedFrames)
      .map(request =>
        Iterable(
          "",
          {minerals  -= request.minerals  ; minerals  }.toString,
          {gas       -= request.gas       ; gas       }.toString,
          {supply    -= request.supply    ; supply / 2}.toString,
          if (request.satisfied)
            "Available"
          else if (request.onSchedule)
            "On schedule"
          else
            "",
          if (request.expectedFrames > 0 && request.expectedFrames < 24 * 60 * 5) (request.expectedFrames/24).toString + " seconds" else "",
            (if (request.minerals > 0)  request.minerals + "m " else "") +
            (if (request.gas      > 0)  request.gas      + "g " else "") +
            (if (request.supply   > 0)  request.supply/2 + "s " else ""),
          "   " + request.owner.toString
            .replace("Build a ", "")
            .replace("Train a ", "")
            .replace("Upgrade ", "")
            .replace("Research ", "")
        ))
          
    var lineLast = Iterable("not happenin")
    val output = new ArrayBuffer[Iterable[String]]()
    output += Vector("Copies", "M", "G", "S", "", "Workers:", With.units.countOurs(MatchWorker).toString)
    rawText.foreach(lineNext => {
      var lineFinal = lineNext
      if (lineFinal.drop(dropColumns) == lineLast.drop(dropColumns)) {
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
      5,
      4 * With.visualization.lineHeightSmall,
      output)
  }
}
