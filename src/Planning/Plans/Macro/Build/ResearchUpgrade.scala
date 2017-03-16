package Planning.Plans.Macro.Build

import Planning.Plans.Allocation._
import Planning.Plan
import Startup.With
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchType
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import bwapi.UpgradeType

class ResearchUpgrade(upgradeType: Upgrade, level: Int) extends Plan {
  
  val currency = new LockCurrencyForUpgrade(upgradeType, level)
  val researcher = new LockUnits {
    unitMatcher.set(new UnitMatchType(upgradeType.whatUpgrades))
    unitCounter.set(UnitCountOne)
  }
  
  override def isComplete: Boolean = With.self.getUpgradeLevel(upgradeType.base) >= level
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
    else if (researcherUnit.upgrading == Upgrades.get(UpgradeType.None)) {
      researcherUnit.baseUnit.upgrade(upgradeType.base)
      currency.isSpent = true
    }
  }
}
