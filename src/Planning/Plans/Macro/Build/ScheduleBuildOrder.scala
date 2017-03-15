package Planning.Plans.Macro.Build

import Planning.Plan
import Startup.With
import Macro.BuildRequests.BuildRequest
import Planning.Composition.Property

class ScheduleBuildOrder extends Plan {
  
  description.set("Schedule a fixed build order")
  
  val buildables = new Property[Iterable[BuildRequest]](List.empty)
  
  override def onFrame() = With.scheduler.request(this, buildables.get)
}
