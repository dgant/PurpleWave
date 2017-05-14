package Planning.Plans.Macro.Expansion

import Lifecycle.With
import Macro.BuildRequests.RequestUnitAnotherOne
import Planning.Plan

class Expand extends Plan {
  
  description.set("Expand")
  
  override def onUpdate() {
    With.scheduler.request(this, Vector(RequestUnitAnotherOne(With.self.townHallClass)))
  }
  
}
