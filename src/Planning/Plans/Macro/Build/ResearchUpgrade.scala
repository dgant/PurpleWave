package Planning.Plans.Macro.Build

import Micro.Intentions.Intention
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plan
import Planning.Plans.Allocation._
import ProxyBwapi.Upgrades.Upgrade
import Startup.With

class ResearchUpgrade(upgrade: Upgrade, level: Int) extends Plan {
  
  val currency = new LockCurrencyForUpgrade(upgrade, level)
  val upgraders = new LockUnits {
    unitCounter.set(UnitCountOne)
    unitMatcher.set(new UnitMatchType(upgrade.whatUpgrades))
  }
  
  description.set("Upgrade " + upgrade + " " + level)
  
  override def isComplete: Boolean = With.self.getMaxUpgradeLevel(upgrade.base) >= level
  override def getChildren: Iterable[Plan] = List (currency, upgraders)
  
  override def onFrame() {
    if (isComplete) return
    
    currency.onFrame()
    if (! currency.isComplete) return
    
    currency.isSpent = false
    upgraders.onFrame()
    upgraders.units.foreach(upgrader => {
      currency.isSpent = upgrader.upgrading == upgrade
      With.executor.intend(new Intention(this, upgrader) { toUpgrade = Some(upgrade) })
    })
  }
}
