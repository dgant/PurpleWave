package Planning.Plans.Macro.Expanding

import Lifecycle.With
import Macro.Buildables.Get
import Planning.{Plan, Property}

class RequireBases(basesInitial: Int = 1) extends Plan {
  
  val basesDesired = new Property(basesInitial)
  
  protected def basesNow: Int = With.geography.ourBases.size
  
  override def onUpdate() {
    val basesAll  = With.geography.ourBases.size
    val goal      = basesDesired.get + basesAll - basesNow
    
    if (goal > 0) {
      With.scheduler.request(this, Get(goal, With.self.townHallClass))
    }
  }
}

