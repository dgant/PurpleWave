package Planning.Plans.Macro.Milestones

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Upgrades.Upgrade

class HaveUpgrade(upgrade: Upgrade, level: Int = 1) extends Plan {
  
  description.set("Require an upgrade")
  
  override def isComplete: Boolean = With.self.getUpgradeLevel(upgrade) >= level
}
