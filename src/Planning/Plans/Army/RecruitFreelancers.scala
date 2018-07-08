package Planning.Plans.Army

import Lifecycle.With
import Planning.Composition.UnitCountEverything
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitMatchers.UnitMatchRecruitableForCombat

class RecruitFreelancers extends Plan {
  
  val freelancers = new LockUnits {
    unitMatcher.set(UnitMatchRecruitableForCombat)
    unitCounter.set(UnitCountEverything)
  }
  
  override def onUpdate() {
    freelancers.acquire(this)
    freelancers.units.foreach(With.squads.addFreelancer)
    With.squads.assignFreelancers()
  }
}
