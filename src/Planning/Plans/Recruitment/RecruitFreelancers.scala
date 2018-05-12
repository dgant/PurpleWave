package Planning.Plans.Recruitment

import Lifecycle.With
import Micro.Squads.{RecruitmentLevel, Squad}
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitMatchers.UnitMatchMobile
import Planning.Plan
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class RecruitFreelancers extends Plan {
  
  val freelancers = new LockUnits {
    unitMatcher.set(UnitMatchMobile)
    unitCounter.set(UnitCountEverything)
  }
  
  override def onUpdate() {
    freelancers.acquire(this)
    val candidates = new mutable.HashSet[FriendlyUnitInfo]
    candidates ++= freelancers.units
  
    def assign(candidate: FriendlyUnitInfo, squad: Squad) {
      squad.recruit(candidate)
      candidates.remove(candidate)
    }
    
    // TODO: Abandon squads which haven't met their minimum requirements,
    // then recycle their units
    
    RecruitmentLevel.values.foreach(recruitmentLevel =>
      With.squads.allByPriority.foreach(squad => {
        val hired = squad.goal.offer(candidates, recruitmentLevel)
        hired.foreach(assign(_, squad))
      }))
    
  }
}
