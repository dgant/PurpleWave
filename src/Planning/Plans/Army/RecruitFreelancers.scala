package Planning.Plans.Army

import Lifecycle.With
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountEverything
import Planning.UnitMatchers.UnitMatchRecruitableForCombat

class RecruitFreelancers extends Prioritized {
  
  val freelancers: LockUnits = new LockUnits
  freelancers.unitMatcher.set(UnitMatchRecruitableForCombat)
  freelancers.unitCounter.set(UnitCountEverything)
  
  def update() {
    freelancers.acquire(this)
    freelancers.units.foreach(With.squads.freelance)
  }
}
