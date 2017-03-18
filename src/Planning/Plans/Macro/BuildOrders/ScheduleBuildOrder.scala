package Planning.Plans.Macro.BuildOrders

import Macro.BuildRequests.BuildRequest
import Planning.Composition.Property
import Planning.Plan
import Startup.With

class ScheduleBuildOrder extends Plan {
  
  description.set("Schedule a fixed build order")
  
  val buildables = new Property[Iterable[BuildRequest]](List.empty)
  
  override def onFrame() = With.scheduler.request(this, buildables.get)
}
