package Planning.Plans.Macro.BuildOrders

import Lifecycle.With
import Macro.Requests.RequestBuildable
import Planning.Plan

class Build(requests: RequestBuildable*) extends Plan {

  override def toString: String = (
    "Build " +
    requests.take(3).map(_.toString).mkString(", ") +
    (if (requests.size > 3) "..." else ""))

  override def onUpdate(): Unit = {
    With.scheduler.requestAll(this, requests)
  }
}
