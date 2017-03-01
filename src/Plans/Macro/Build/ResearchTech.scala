package Plans.Macro.Build

import Plans.Allocation.{LockCurrency, LockCurrencyForTech, LockUnits, LockUnitsExactly}
import Plans.Plan
import Startup.With
import Strategies.UnitMatchers.UnitMatchType
import Utilities.Property
import bwapi.TechType

class ResearchTech(techType: TechType) extends Plan {
  
  val currency   = new Property[LockCurrency](new LockCurrencyForTech(techType))
  val researcher = new Property[LockUnits](new LockUnitsExactly { unitMatcher.set(new UnitMatchType(techType.whatResearches)) })
  
  override def isComplete: Boolean = { With.game.self.hasResearched(techType) }
  override def getChildren: Iterable[Plan] = { List (currency.get, researcher.get) }
  
  override def onFrame() {
    currency.get.onFrame()
    if ( ! currency.get.isComplete) {
      return
    }
    
    researcher.get.onFrame()
    if ( ! researcher.get.isComplete || researcher.get.units.isEmpty) {
      return
    }
    
    val researcherUnit = researcher.get.units.head
    if (researcherUnit.teching == techType) {
      currency.get.isSpent = true
    }
    else if (researcherUnit.teching == TechType.None) {
      researcherUnit.baseUnit.research(techType)
      currency.get.isSpent = true
    }
  }
}
