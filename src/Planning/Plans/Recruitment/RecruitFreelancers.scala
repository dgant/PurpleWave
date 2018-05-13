package Planning.Plans.Recruitment

import Lifecycle.With
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitMatchers.UnitMatchMobile
import Planning.Plan

class RecruitFreelancers extends Plan {
  
  val freelancers = new LockUnits {
    unitMatcher.set(UnitMatchMobile)
    unitCounter.set(UnitCountEverything)
  }
  
  override def onUpdate() {
    freelancers.acquire(this)
    freelancers.units.foreach(With.squads.addFreelancer)
    With.squads.assignFreelancers()
  }
}
