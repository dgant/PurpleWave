package Planning.Plans.Predicates.Milestones

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Upgrades.Upgrade

class UpgradeComplete(upgrade: Upgrade, level: Int = 1, withinFrames: Int = 0) extends Plan {
  
  description.set("Require an upgrade")
  
  override def isComplete: Boolean =
    With.self.getUpgradeLevel(upgrade) >= level ||
    (withinFrames > 0 && With.units.ours.exists(unit =>
      unit.upgrading
      && unit.upgradingType == upgrade
      && unit.remainingUpgradeFrames <= withinFrames))
}
