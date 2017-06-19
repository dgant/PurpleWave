package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Rendering.DrawScreen
import Lifecycle.With

object ScreenResources {
  def render() {
    DrawScreen.table(
      285,
      31,
      With.bank.prioritizedRequests
        .take(20)
        .map(request =>
          Iterable(
            if (request.isSatisfied) "X" else "",
            if (request.expectedFrames > 0 && request.expectedFrames < 24 * 60 * 5) (request.expectedFrames/24).toString + " seconds" else "",
            (if (request.minerals > 0)  request.minerals + "m " else "") +
            (if (request.gas      > 0)  request.gas      + "g " else "") +
            (if (request.supply   > 0)  request.supply   + "s " else ""),
            request.owner.toString
          )))
  }
}
