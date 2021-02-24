package Tactics

import Lifecycle.With
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountEverything
import Planning.UnitMatchers.MatchRecruitableForCombat

class RecruitFreelancers extends Prioritized {
  
  val freelancers: LockUnits = new LockUnits
  freelancers.matcher.set(MatchRecruitableForCombat)
  freelancers.counter.set(CountEverything)
  
  def update() {
    freelancers.acquire(this)
    freelancers.units.foreach(With.squads.freelance)
  }
}
