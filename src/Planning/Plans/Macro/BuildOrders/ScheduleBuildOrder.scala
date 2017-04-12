package Planning.Plans.Macro.BuildOrders

import Macro.BuildRequests.BuildRequest
import Planning.Composition.Property
import Planning.Plan
import Lifecycle.With

class ScheduleBuildOrder(initialBuildables:Seq[BuildRequest] = Vector.empty) extends Plan {
  
  description.set("Schedule a fixed build order")
  
  val buildables = new Property[Seq[BuildRequest]](initialBuildables)
  
  override def update() = With.scheduler.request(this, buildables.get)
}
