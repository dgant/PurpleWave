package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.DrawScreen
import Startup.With

object VisualizeResources {
  def render() {
    DrawScreen.column(
      455,
      31,
      With.bank.prioritizedRequests
        .take(20)
        .map(r =>
          (if (r.isSatisfied) "X " else "  ") ++
            (if (r.minerals > 0)  r.minerals  .toString ++ "m " else "") ++
            (if (r.gas      > 0)  r.gas       .toString ++ "g " else "") ++
            (if (r.supply   > 0)  r.supply    .toString ++ "s " else "") ++
            r.owner.toString)
        .mkString("\n"))
  }
}
