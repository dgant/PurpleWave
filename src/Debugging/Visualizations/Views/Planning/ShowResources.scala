package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowResources extends View {
  override def renderScreen() {
    DrawScreen.table(
      300,
      4 * With.visualization.lineHeightSmall,
      With.bank.prioritizedRequests
        .take(25)
        .map(request =>
          Iterable(
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
          )))
  }
}
