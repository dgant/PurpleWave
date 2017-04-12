package Planning.Plans.Macro.Build

import Micro.Intent.Intention
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plan
import Planning.Composition.ResourceLocks.{LockCurrencyForTech, LockUnits}
import ProxyBwapi.Techs.Tech
import Lifecycle.With

class ResearchTech(tech: Tech) extends Plan {
  
  val currency = new LockCurrencyForTech(tech)
  val techers = new LockUnits {
    unitCounter.set(UnitCountOne)
    unitMatcher.set(new UnitMatchType(tech.whatResearches))
  }
  
  description.set("Tech " + tech)
  
  override def isComplete: Boolean = With.self.hasResearched(tech)
  
  override def onFrame() {
    if (isComplete) return
    
    currency.acquire(this)
    if (! currency.satisfied) return
  
    currency.isSpent = false
    techers.acquire(this)
    techers.units.foreach(techer => {
      currency.isSpent = techer.teching == tech
      With.executor.intend(new Intention(this, techer) { toTech = Some(tech) })
    })
  }
}
