package Planning.Plans.Macro.Expanding

import Debugging.English
import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.Property
import Planning.Plan
import Planning.Plans.Macro.Milestones.AllMiningBases

class RequireMiningBases(basesInitial: Int = 1) extends Plan {
  
  override def toString: String = "Require " + basesDesired.get + English.pluralize(" mining base", basesDesired.get)
  
  val basesDesired = new Property(basesInitial)
  
  override def onUpdate() {
    val basesAll    = With.geography.ourBases.size
    val basesMining = AllMiningBases().size
    val goal        = basesDesired.get + basesAll - basesMining
    
    if (goal > 0) {
      With.scheduler.request(this, RequestAtLeast(goal, With.self.townHallClass))
    }
  }
}

