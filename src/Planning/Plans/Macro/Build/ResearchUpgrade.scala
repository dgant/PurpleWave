package Planning.Plans.Macro.Build

import Lifecycle.With
import Macro.Scheduling.Project
import Micro.Agency.Intention
import Planning.Composition.ResourceLocks._
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Plan
import ProxyBwapi.UnitClass.UnitClasses
import ProxyBwapi.Upgrades.Upgrade

class ResearchUpgrade(upgrade: Upgrade, level: Int) extends Plan {
  
  val upgraderClass = upgrade.whatUpgrades
  val currency = new LockCurrencyForUpgrade(upgrade, level)
  val upgraders = new LockUnits {
    unitCounter.set(UnitCountOne)
    unitMatcher.set(upgraderClass)
  }
  
  description.set("Upgrade " + upgrade + " " + level)
  
  override def isComplete: Boolean = With.self.getUpgradeLevel(upgrade) >= level
  
  override def onUpdate() {
    if (isComplete) return
    
    val requiredClasses = (upgrade.whatsRequired.get(level).toVector :+ upgraderClass).filterNot(_ == UnitClasses.None)
    
    // Don't even stick a projected expenditure in the queue if we're this far out.
    if (requiredClasses.exists(c => ! With.units.existsOurs(c))) return
    
    currency.framesPreordered = requiredClasses.map(Project.framesToUnits(_)).max
    currency.acquire(this)
    currency.isSpent = With.units.ours.exists(upgrader => upgrader.upgrading && upgrader.upgradingType == upgrade)
    if ( ! currency.satisfied) return
    
    upgraders.acquire(this)
    upgraders.units.foreach(_.agent.intend(this, new Intention { toUpgrade = Some(upgrade) }))
  }
}
