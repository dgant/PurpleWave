package Planning.Plans.Macro.Upgrades

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plan

class UpgradeContinuously(upgrade: ProxyBwapi.Upgrades.Upgrade, maxLevel: Int = 3) extends Plan {
  
  description.set("Upgrade " + upgrade + " up to level " + maxLevel)
  
  override def isComplete: Boolean = With.self.getUpgradeLevel(upgrade) >= Math.min(maxLevel, upgrade.levels.size)
  
  override def onUpdate() {
    if (isComplete) return
    if ( ! With.units.existsOurs(upgrade.whatUpgrades)) return
    With.scheduler.request(this, Get(upgrade, With.self.getUpgradeLevel(upgrade) + 1))
  }
}
