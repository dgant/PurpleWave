package Planning.Plans.Macro.Build

import Micro.Intent.Intention
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plan
import Planning.Composition.ResourceLocks._
import ProxyBwapi.Upgrades.Upgrade
import Lifecycle.With

class ResearchUpgrade(upgrade: Upgrade, level: Int) extends Plan {
  
  val currency = new LockCurrencyForUpgrade(upgrade, level)
  val upgraders = new LockUnits {
    unitCounter.set(UnitCountOne)
    unitMatcher.set(new UnitMatchType(upgrade.whatUpgrades))
  }
  
  description.set("Upgrade " + upgrade + " " + level)
  
  override def isComplete: Boolean = With.self.getUpgradeLevel(upgrade) >= level
  
  override def update() {
    if (isComplete) return
    
    currency.acquire(this)
    if (! currency.satisfied) return
    
    currency.isSpent = false
    upgraders.acquire(this)
    upgraders.units.foreach(upgrader => {
      currency.isSpent = upgrader.upgrading == upgrade
      With.executor.intend(new Intention(this, upgrader) { toUpgrade = Some(upgrade) })
    })
  }
}
