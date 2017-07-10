package Planning.Plans.Macro.Build

import Micro.Intent.Intention
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plan
import Planning.Composition.ResourceLocks.{LockCurrencyForTech, LockUnits}
import ProxyBwapi.Techs.Tech
import Lifecycle.With
import Macro.Scheduling.Project

class ResearchTech(tech: Tech) extends Plan {

  val techerClass = tech.whatResearches
  val currency = new LockCurrencyForTech(tech)
  val techers = new LockUnits {
    unitCounter.set(UnitCountOne)
    unitMatcher.set(UnitMatchType(techerClass))
  }
  
  description.set("Tech " + tech)
  
  override def isComplete: Boolean = With.self.hasTech(tech)
  
  override def onUpdate() {
    if (isComplete) return
    
    currency.framesAhead = Project.framesToUnits(techerClass)
    currency.acquire(this)
    currency.isSpent = With.units.ours.exists(techer => techer.teching && techer.techingType == tech)
    if ( ! currency.satisfied) return
  
    techers.acquire(this)
    techers.units.foreach(techer => {
      With.executor.intend(new Intention(this, techer) { toTech = Some(tech) })
    })
  }
}
