package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.Upgrades.Upgrade

class EnemyHasUpgrade(upgrade: Upgrade, level: Int = 1) extends Predicate {
  
  override def apply: Boolean = With.enemies.exists(_.getUpgradeLevel(upgrade) >= level)
  
}
