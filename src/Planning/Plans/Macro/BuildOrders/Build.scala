package Planning.Plans.Macro.BuildOrders

import Lifecycle.With
import Macro.Buildables.RequestProduction
import Planning.Plan

class Build(requests: RequestProduction*) extends Plan {

  override def toString: String = (
    "Build " +
    requests.take(3).map(_.toString).mkString(", ") +
    (if (requests.size > 3) "..." else ""))

  override def onUpdate() {
    With.scheduler.requestAll(this, requests)
  }
}
