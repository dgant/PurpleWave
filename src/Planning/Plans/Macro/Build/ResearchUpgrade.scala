package Planning.Plans.Macro.Build

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks._
import Planning.UnitCounters.UnitCountOne
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchIdle}
import Planning.UnitPreferences.UnitPreferIdle
import ProxyBwapi.UnitClasses.UnitClasses
import ProxyBwapi.Upgrades.Upgrade

class ResearchUpgrade(upgrade: Upgrade, level: Int) extends Plan {
  
  val upgraderClass = upgrade.whatUpgrades
  val currency = new LockCurrencyForUpgrade(upgrade, level)
  val upgraders = new LockUnits {
    unitCounter.set(UnitCountOne)
    unitMatcher.set(UnitMatchAnd(upgraderClass, UnitMatchIdle))
    unitPreference.set(UnitPreferIdle)
  }
  
  description.set("Upgrade " + upgrade + " " + level)
  
  override def isComplete: Boolean = With.self.getUpgradeLevel(upgrade) >= level
  
  override def onUpdate() {
    if (isComplete) return
    if (With.units.ours.exists(u => u.upgradingType == upgrade && ! upgraders.units.contains(u))) return
    
    val requiredClasses = (upgrade.whatsRequired.get(level).toVector :+ upgraderClass).filterNot(_ == UnitClasses.None)
    
    // Don't even stick a projected expenditure in the queue if we're this far out.
    if (requiredClasses.exists(c => ! With.units.existsOurs(c))) return
    
    currency.framesPreordered = (
      upgraders.units.view.map(_.remainingOccupationFrames)
      ++ requiredClasses.map(With.projections.unit)).max
    currency.acquire(this)
    currency.isSpent = With.units.ours.exists(upgrader => upgrader.upgrading && upgrader.upgradingType == upgrade)
    if ( ! currency.satisfied) return
    
    upgraders.acquire(this)
    upgraders.units.foreach(_.agent.intend(this, new Intention { toUpgrade = Some(upgrade) }))
  }
}
