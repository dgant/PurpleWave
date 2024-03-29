package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plan

class UpgradeContinuously(upgrade: ProxyBwapi.Upgrades.Upgrade, maxLevel: Int = 3) extends Plan {
  
  override def onUpdate(): Unit = {
    if (With.self.getUpgradeLevel(upgrade) >= Math.min(maxLevel, upgrade.levels.size)) return
    if ( ! With.units.existsOurs(upgrade.whatUpgrades)) return
    With.scheduler.request(this, Get(upgrade, With.self.getUpgradeLevel(upgrade) + 1))
  }

  override def toString = f"Upgrade $upgrade up to level $maxLevel"
}
