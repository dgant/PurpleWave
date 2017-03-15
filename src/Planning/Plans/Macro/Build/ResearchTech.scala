package Planning.Plans.Macro.Build

import Planning.Plans.Allocation.{LockCurrencyForTech, LockUnits}
import Planning.Plan
import Startup.With
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchType
import bwapi.TechType

class ResearchTech(techType: TechType) extends Plan {
  
  val currency = new LockCurrencyForTech(techType)
  val researcher = new LockUnits {
    unitCounter.set(UnitCountOne)
    unitMatcher.set(new UnitMatchType(techType.whatResearches))
  }
  
  override def isComplete: Boolean = With.self.hasResearched(techType)
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
    if (researcherUnit.teching == techType) {
      currency.isSpent = true
    }
    else if (researcherUnit.teching == TechType.None) {
      researcherUnit.baseUnit.research(techType)
      currency.isSpent = true
    }
  }
}
