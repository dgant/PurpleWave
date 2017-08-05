package Planning.Plans.Recruitment

import Lifecycle.With
import Micro.Squads.Squad
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitMatchers.UnitMatchMobile
import Planning.Plan
import Utilities.ByOption

import scala.collection.mutable

class RecruitFreelancers extends Plan {
  
  val freelancers = new LockUnits {
    unitMatcher.set(UnitMatchMobile)
    unitCounter.set(UnitCountEverything)
  }
  
  override def onUpdate() {
  
    val squads = With.squads.squadsByPriority.filter(_.recruits.nonEmpty)
    
    if (squads.isEmpty) return
    
    freelancers.acquire(this)
    val candidates = new mutable.HashSet[Resume] ++ freelancers.units.map(Resume)
    
    def assign(candidate: Resume, squad: Squad) {
      squad.recruit(candidate.unit)
      candidates.remove(candidate)
    }
    
    def distance(candidate: Resume, squad: Squad) {
      candidate.unit.pixelDistanceFast(squad.centroid)
    }
    
    def recruitBest(squad: Squad, accept: (Resume) => Boolean) {
      ByOption.min(candidates.toSeq.filter(accept))(distance(_, squad)).foreach(assign(_, squad))
    }
    
    // For utility jobs: Give each squad a chance to recruit (ie. make sure nobody hogs all the detectors)
    squads.foreach(squad => {
      if (squad.needsDetectors) recruitBest(squad, _.detects)
      if (squad.needsTransport) recruitBest(squad, _.transports)
      if (squad.needsSpotters)  recruitBest(squad, _.spots)
      if (squad.needsRepairers) recruitBest(squad, _.repairs)
      if (squad.needsHealers)   recruitBest(squad, _.heals)
      if (squad.needsBuilders)  recruitBest(squad, _.builds)
    })
    
    // Join the nearest squad.
    while (candidates.nonEmpty) {
      val candidate = candidates.head
      assign(candidate, squads.minBy(distance(candidate, _)))
    }
  }
}
