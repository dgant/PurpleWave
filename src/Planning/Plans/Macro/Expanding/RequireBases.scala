package Planning.Plans.Macro.Expanding

import Debugging.English
import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.{Plan, Property}

class RequireBases(basesInitial: Int = 1) extends Plan {
  
  val basesDesired = new Property(basesInitial)
  
  description.set("Require " + basesDesired.get + English.pluralize(" base", basesDesired.get))
  
  protected def basesNow: Int = {
    With.geography.ourBases.size
  }
  
  override def onUpdate() {
    val basesAll  = With.geography.ourBases.size
    val goal      = basesDesired.get + basesAll - basesNow
    
    if (goal > 0) {
      With.scheduler.request(this, Get(goal, With.self.townHallClass))
    }
  }
}

