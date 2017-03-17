package Planning.Plans.Macro.Build

import Micro.Intentions.Intention
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plan
import Planning.Plans.Allocation.{LockCurrencyForTech, LockUnits}
import ProxyBwapi.Techs.Tech
import Startup.With

class ResearchTech(tech: Tech) extends Plan {
  
  val currency = new LockCurrencyForTech(tech)
  val techers = new LockUnits {
    unitCounter.set(UnitCountOne)
    unitMatcher.set(new UnitMatchType(tech.whatResearches))
  }
  
  description.set("Tech " + tech)
  
  override def isComplete: Boolean = With.self.hasResearched(tech.base)
  override def getChildren: Iterable[Plan] = List (currency, techers)
  
  override def onFrame() {
    if (isComplete) return
    
    currency.onFrame()
    if (! currency.isComplete) return
  
    currency.isSpent = false
    techers.onFrame()
    techers.units.foreach(techer => {
      currency.isSpent = techer.teching == tech
      With.executor.intend(new Intention(this, techer) { toTech = Some(tech) })
    })
  }
}
