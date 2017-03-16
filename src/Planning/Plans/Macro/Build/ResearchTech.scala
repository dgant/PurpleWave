package Planning.Plans.Macro.Build

import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plan
import Planning.Plans.Allocation.{LockCurrencyForTech, LockUnits}
import ProxyBwapi.Techs.{NoTech, Tech}
import Startup.With

class ResearchTech(tech: Tech) extends Plan {
  
  val currency = new LockCurrencyForTech(tech)
  val researcher = new LockUnits {
    unitCounter.set(UnitCountOne)
    unitMatcher.set(new UnitMatchType(tech.whatResearches))
  }
  
  override def isComplete: Boolean = With.self.hasResearched(tech.base)
  override def getChildren: Iterable[Plan] = List (currency, researcher)
  
  override def onFrame() {
    currency.onFrame()
    if ( ! currency.isComplete) {
      return
    }
    
    researcher.onFrame()
    if ( ! researcher.isComplete || researcher.units.isEmpty) {
      return
    }
    
    val researcherUnit = researcher.units.head
    if (researcherUnit.teching == tech) {
      currency.isSpent = true
    }
    else if (researcherUnit.teching == NoTech) {
      researcherUnit.baseUnit.research(tech.base)
      currency.isSpent = true
    }
  }
}
