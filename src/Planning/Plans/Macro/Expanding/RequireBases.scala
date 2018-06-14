package Planning.Plans.Macro.Expanding

import Debugging.English
import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Composition.Property
import Planning.Plan

class RequireBases(basesInitial: Int = 1) extends Plan {
  
  description.set("Require " + basesDesired.get + English.pluralize(" bases", basesDesired.get))
  
  val basesDesired = new Property(basesInitial)
  
  protected def basesNow: Int = {
    With.geography.ourBases.size
  }
  
  override def onUpdate() {
    val basesAll    = With.geography.ourBases.size
    val goal        = basesDesired.get + basesAll - basesNow
    
    if (goal > 0) {
      With.scheduler.request(this, Get(goal, With.self.townHallClass))
    }
  }
}

