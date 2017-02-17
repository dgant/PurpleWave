package Plans.Generic.Macro

import Plans.Generic.Allocation._
import Plans.Plan
import Startup.With
import Strategies.UnitMatchers.UnitMatchType
import Types.Property
import bwapi.UpgradeType

class ResearchUpgrade(upgradeType: UpgradeType, level: Int) extends Plan {
  
  val currency   = new Property[LockCurrency](new LockCurrencyForUpgrade(upgradeType, level))
  val researcher = new Property[LockUnits](new LockUnitsExactly { unitMatcher.set(new UnitMatchType(upgradeType.whatUpgrades)) })
  
  override def isComplete: Boolean = { With.game.self.getUpgradeLevel(upgradeType) >= level }
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
    if (researcherUnit.getUpgrade == upgradeType) {
      currency.get.isSpent = true
    }
    else if (researcherUnit.getUpgrade == UpgradeType.None) {
      researcherUnit.upgrade(upgradeType)
      currency.get.isSpent = true
    }
  }
}
