package Planning.Plans.Army

import Lifecycle.With
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.{UnitCountEverything, UnitCounter}
import Planning.UnitMatchers.{UnitMatchRecruitableForCombat, UnitMatcher}

class RecruitFreelancers(
  initialMatcher: UnitMatcher = UnitMatchRecruitableForCombat,
  initialCounter: UnitCounter = UnitCountEverything) extends Plan {
  
  val freelancers: LockUnits = new LockUnits {
    unitMatcher.set(initialMatcher)
    unitCounter.set(initialCounter)
  }
  
  override def onUpdate() {
    freelancers.acquire(this)
    freelancers.units.foreach(With.squads.advertise)
  }
}
