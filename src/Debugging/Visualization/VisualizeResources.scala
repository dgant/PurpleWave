package Debugging.Visualization

import Startup.With

object VisualizeResources {
  def render() {
    DrawScreen.column(
      305,
      31,
      With.bank.prioritizedRequests
        .take(8)
        .map(r =>
          (if (r.isSatisfied) "X " else "  ") ++
            (if (r.minerals > 0)  r.minerals  .toString ++ "m " else "") ++
            (if (r.gas      > 0)  r.gas       .toString ++ "g " else "") ++
            (if (r.supply   > 0)  r.supply    .toString ++ "s " else ""))
        .mkString("\n"))
  }
}
