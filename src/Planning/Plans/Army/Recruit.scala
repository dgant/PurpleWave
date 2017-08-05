package Planning.Plans.Army

import Lifecycle.With
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Plan

class Recruit extends Plan {
  
  val freelancers = new LockUnits {
    unitMatcher.set(UnitMatchWorkers)
    unitCounter.set(UnitCountEverything)
  }
  
  override def onUpdate() {
    
    val squads = With.squads.all.map(squad => (squad, squad.centroid))
    
    if (squads.isEmpty) return
    
    freelancers.acquire(this)
    freelancers.units.foreach(freelancer => {
      squads.minBy(squad => freelancer.pixelDistanceFast(squad._2))._1.recruit(freelancer)
    })
  }
}
