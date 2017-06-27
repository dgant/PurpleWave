package Planning.Plans.Macro.Expanding

import Debugging.English
import Lifecycle.With
import Macro.BuildRequests.RequestUnitAtLeast
import Planning.Composition.Property
import Planning.Plan

class RequireMiningBases(basesInitial: Int = 1) extends Plan {
  
  override def toString: String = "Require " + basesDesired.get + English.pluralize(" mining base", basesDesired.get)
  
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

