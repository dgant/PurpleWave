package Planning.Plans.Macro.Milestones

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Upgrades.Upgrade

class HaveUpgrade(upgrade: Upgrade, level: Int = 1, withinFrames: Int = 0) extends Plan {
  
  description.set("Require an upgrade")
  
  override def isComplete: Boolean =
    With.self.getUpgradeLevel(upgrade) >= level ||
    (withinFrames > 0 && With.units.ours.exists(unit =>
      unit.researching &&
      unit.upgradingType == upgrade &&
      unit.framesBeforeUpgradeComplete <= withinFrames))
}
