package Planning.Plans.Macro.Build

import Lifecycle.With
import Micro.Intent.Intention
import Planning.Composition.ResourceLocks._
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plan
import ProxyBwapi.Upgrades.Upgrade

class ResearchUpgrade(upgrade: Upgrade, level: Int) extends Plan {
  
  val currency = new LockCurrencyForUpgrade(upgrade, level)
  val upgraders = new LockUnits {
    unitCounter.set(UnitCountOne)
    unitMatcher.set(new UnitMatchType(upgrade.whatUpgrades))
  }
  
  description.set("Upgrade " + upgrade + " " + level)
  
  override def isComplete: Boolean = With.self.getUpgradeLevel(upgrade) >= level
  
  override def onUpdate() {
    if (isComplete) return
    
    currency.acquire(this)
    if (! currency.satisfied) return
    
    currency.isSpent = false
    upgraders.acquire(this)
    upgraders.units.foreach(upgrader => {
      currency.isSpent = upgrader.upgrading
      With.executor.intend(new Intention(this, upgrader) { toUpgrade = Some(upgrade) })
    })
  }
}
