package Planning.Predicates.Milestones

import Planning.Predicates.{MacroFacts, Predicate}
import ProxyBwapi.Upgrades.Upgrade

case class EnemyHasUpgrade(upgrade: Upgrade, level: Int = 1) extends Predicate {
  override def apply: Boolean = MacroFacts.enemyHasUpgrade(upgrade, level)
}
