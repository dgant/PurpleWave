package Planning.Plans.Macro.BuildOrders

import Macro.Requests.RequestBuildable
import Planning.Plan

/**
  * Requests buildables without replacing non-tech units
  */
class BuildOrder(requests: RequestBuildable*) extends Plan {
  override def onUpdate(): Unit = {
    requests.foreach(BuildOnce(this, _))
  }
}
