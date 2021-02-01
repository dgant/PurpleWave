package Planning.Plans.Army

import Lifecycle.With
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountEverything
import Planning.UnitMatchers.UnitMatchRecruitableForCombat

class RecruitFreelancers extends Plan {
  
  val freelancers: LockUnits = new LockUnits
  freelancers.unitMatcher.set(UnitMatchRecruitableForCombat)
  freelancers.unitCounter.set(UnitCountEverything)
  
  override def onUpdate() {
    freelancers.acquire(this)
    freelancers.units.foreach(With.squads.freelance)
  }
}
