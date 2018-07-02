package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.Upgrades.Upgrade

class UpgradeComplete(upgrade: Upgrade, level: Int = 1, withinFrames: Int = 0) extends Predicate {
  
  override def isComplete: Boolean = (
    With.self.getUpgradeLevel(upgrade) >= level
    || (
      With.self.getUpgradeLevel(upgrade) == level - 1
      && withinFrames > 0
      && With.units.ours.exists(unit =>
        unit.upgrading
        && unit.upgradingType == upgrade
        && unit.remainingUpgradeFrames <= withinFrames)))
}
