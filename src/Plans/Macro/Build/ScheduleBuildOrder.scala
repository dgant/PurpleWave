package Plans.Macro.Build

import Plans.Plan
import Startup.With
import Types.Buildable.Buildable
import Types.Property

class ScheduleBuildOrder extends Plan {
  
  description.set(Some("Schedule a fixed build order"))
  
  val buildables = new Property[Iterable[Buildable]](List.empty)
  
  override def onFrame() { With.scheduler.request(this, buildables.get) }
}
