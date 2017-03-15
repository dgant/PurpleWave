package Plans.Macro.Build

import Plans.Plan
import Startup.With
import Types.BuildRequest.BuildRequest
import Utilities.Property

class ScheduleBuildOrder extends Plan {
  
  description.set("Schedule a fixed build order")
  
  val buildables = new Property[Iterable[BuildRequest]](List.empty)
  
  override def onFrame() = With.scheduler.request(this, buildables.get)
}
