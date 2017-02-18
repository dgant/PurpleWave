package Plans.Macro.Build

import Plans.Plan
import Types.Buildable.Buildable
import Types.Property

class ScheduleBuildOrder extends Plan {
  
  description.set(Some("Schedule a fixed build order"))
  
  val buildables = new Property[Iterable[Buildable]](List.empty)
  
}
