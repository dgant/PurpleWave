package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Macro.BuildRequests.RequestUnitAtLeast
import Planning.Composition.Property
import Planning.Plan

class BuildMiningBases(basesInitial: Int = 1) extends Plan {
  
  description.set("Builds a certain number of mining bases")
  
  val basesDesired = new Property(basesInitial)
  
  override def onUpdate() {
    val bases       = With.geography.ourBases.size
    val miningBases = With.geography.ourBases.count(_.mineralsLeft > With.configuration.maxMineralsBeforeMinedOut)
    val goal        = basesDesired.get + bases - miningBases
    
    if (goal > 0) {
      With.scheduler.request(this, RequestUnitAtLeast(goal, With.self.townHallClass))
    }
  }
}

