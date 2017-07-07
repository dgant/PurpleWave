package Planning.Plans.Macro.Upgrades

import Lifecycle.With
import Macro.BuildRequests.RequestUpgrade
import Planning.Plan
import ProxyBwapi.Upgrades.Upgrade

class UpgradeContinuously(upgrade: Upgrade, maxLevel: Int = 3) extends Plan {
  
  description.set("Upgrade " + upgrade + " up to level " + maxLevel)
  
  override def isComplete: Boolean = With.self.getUpgradeLevel(upgrade) >= maxLevel
  
  override def onUpdate() {
    if (isComplete) return
    With.scheduler.request(this, RequestUpgrade(upgrade, With.self.getUpgradeLevel(upgrade) + 1))
  }
}
