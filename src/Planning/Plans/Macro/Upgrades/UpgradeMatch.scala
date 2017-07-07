package Planning.Plans.Macro.Upgrades

import Lifecycle.With
import Macro.BuildRequests.RequestUpgrade
import Planning.Plan
import ProxyBwapi.Upgrades.Upgrade

class UpgradeMatch(ourUpgrade: Upgrade, enemyUpgrade: Upgrade, delta: Int = 0) extends Plan {
  
  description.set("Upgrade " + ourUpgrade + " to be enemy's " + enemyUpgrade + (if(delta != 0) " " + delta else ""))
  
  override def isComplete: Boolean = {
    With.self.getUpgradeLevel(ourUpgrade) >= levelRequired
  }
  
  override def onUpdate() {
    if (isComplete) return
    With.scheduler.request(this, RequestUpgrade(ourUpgrade, With.self.getUpgradeLevel(ourUpgrade) + 1))
  }
  
  private def levelRequired: Int = {
    Math.min(3, With.enemies.map(_.getUpgradeLevel(enemyUpgrade)).max + delta)
  }
}
