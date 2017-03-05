package Plans.Macro.Build

import Plans.Allocation._
import Plans.Plan
import Startup.With
import Strategies.UnitCounters.UnitCountOne
import Strategies.UnitMatchers.UnitMatchType
import bwapi.UpgradeType

class ResearchUpgrade(upgradeType: UpgradeType, level: Int) extends Plan {
  
  val currency = new LockCurrencyForUpgrade(upgradeType, level)
  val researcher = new LockUnits {
    unitMatcher.set(new UnitMatchType(upgradeType.whatUpgrades))
    unitCounter.set(UnitCountOne)
  }
  
  override def isComplete: Boolean = { With.game.self.getUpgradeLevel(upgradeType) >= level }
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
    if (researcherUnit.upgrading == upgradeType) {
      currency.isSpent = true
    }
    else if (researcherUnit.upgrading == UpgradeType.None) {
      researcherUnit.baseUnit.upgrade(upgradeType)
      currency.isSpent = true
    }
  }
}
